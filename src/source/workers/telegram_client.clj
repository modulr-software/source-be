(ns source.workers.telegram-client
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [source.config :as conf]
            [source.util :as util]
            [source.db.util :as db.util]
            [source.workers.bundles :as bundles]))

(def ^:private default-base-url "https://api.telegram.org")

(defprotocol TelegramClient
  "A Telegram HTTP client backed by the Telegram Bot API.

  The client is configured with a :chat-id and :token at
  instantiation; both may be overridden on a per-call basis via the
  :chat-id / :token keys in opts.

  All opts maps use kebab-case keys (e.g. :chat-id, :reply-parameters,
  :parse-mode); they are converted to Telegram's snake_case format before the
  request is sent. Authentication is via the bot token embedded in the URL
  path (https://api.telegram.org/bot<token>/METHOD_NAME) — no auth header is
  used.

  The server config (:base-url, :token, :chat-id) is held in an atom and can
  be inspected or replaced at runtime via get-config / set-config!.

  Common opts shared by most send methods (not repeated in each docstring):
  :chat-id, :message-thread-id, :business-connection-id, :disable-notification,
  :protect-content, :allow-paid-broadcast, :message-effect-id, :reply-parameters,
  :reply-markup."

  ;; ─── Config management ───
  (get-config [this]
    "Return the current server config map (:base-url, :token, :chat-id).")
  (set-config! [this cfg]
    "Replace the entire server config map. cfg: :base-url, :token, :chat-id.")

  ;; ─── Sending messages ───
  (send-message [this opts]
    "Send a text message. opts: :chat-id, :text, :parse-mode, :entities,
    :link-preview-options, plus the common opts.")
  (send-photo [this opts]
    "Send a photo. opts: :chat-id, :photo, :caption, :parse-mode,
    :caption-entities, :show-caption-above-media, :has-spoiler, plus the
    common opts.")
  (send-video [this opts]
    "Send a video. opts: :chat-id, :video, :duration, :width, :height,
    :thumbnail, :cover, :start-timestamp, :caption, :parse-mode,
    :caption-entities, :show-caption-above-media, :has-spoiler,
    :supports-streaming, plus the common opts.")
  (send-animation [this opts]
    "Send an animation (GIF or H.264 without sound). opts: :chat-id,
    :animation, :duration, :width, :height, :thumbnail, :caption,
    :parse-mode, :caption-entities, :show-caption-above-media, :has-spoiler,
    plus the common opts.")
  (send-audio [this opts]
    "Send an audio file (displayed in the music player). opts: :chat-id,
    :audio, :caption, :parse-mode, :caption-entities, :duration, :performer,
    :title, :thumbnail, plus the common opts.")
  (send-poll [this opts]
    "Send a native poll. opts: :chat-id, :question, :question-parse-mode,
    :question-entities, :options, :is-anonymous, :type,
    :allows-multiple-answers, :correct-option-ids, :explanation,
    :explanation-parse-mode, :explanation-entities, :open-period,
    :close-date, :is-closed, plus the common opts (no :message-effect-id).")
  (send-chat-action [this opts]
    "Tell the user something is happening on the bot's side. opts: :chat-id,
    :action, :message-thread-id, :business-connection-id. Returns true on
    success.")
  (get-chat [this opts]
    "Get info about a chat. opts: :chat-id, :business-connection-id.
    Returns the Chat object on success; throws if the chat-id is invalid
    or the bot is not a member — use this to validate a chat-id."))

;; ─── Key conversion (kebab-case -> snake_case) ───

(defn- convert-key [k]
  (-> k name (str/replace #"-" "_") keyword))

(defn- to-telegram
  "Recursively converts kebab-case keys in maps/vectors to Telegram's
  snake_case format."
  [x]
  (cond
    (map? x)    (reduce-kv (fn [m k v] (assoc m (convert-key k) (to-telegram v))) {} x)
    (vector? x) (mapv to-telegram x)
    :else       x))

;; ─── Helpers ───

(defn- try-parse-json [s]
  (try (json/read-str s :key-fn keyword) (catch Exception _ s)))

(defn- body-from-opts [opts]
  (fn [] (to-telegram opts)))

(defn- get-me!
  "POST {base-url}/bot{token}/getMe to verify the token is valid and the
  server is reachable. Throws if the request errors or returns a non-2xx
  status."
  [base-url token]
  (let [resp @(http/request {:method :post
                             :url (str base-url "/bot" token "/getMe")
                             :timeout 5000})]
    (when (or (:error resp)
              (not (<= 200 (:status resp) 299)))
      (throw (ex-info "Telegram bot token is not valid or server is not reachable"
                      {:base-url base-url
                       :status (:status resp)
                       :error (:error resp)})))))

(defn- resolve-chat
  "Resolve the chat-id for a request. opts may override the client's
  configured :chat-id via :chat-id."
  [cfg opts]
  (assoc opts :chat-id (or (:chat-id opts) (:chat-id cfg))))

;; ─── Request middleware (req -> req) ───

(defn with-cfg [cfg]
  (fn [req] (assoc req :cfg cfg)))

(defn with-path [req]
  (assoc req :path (:path-template req)))

(defn with-url [req]
  (assoc req :url (str (-> req :cfg :base-url)
                       "/bot" (or (:token req) (-> req :cfg :token))
                       (:path req))))

(defn with-content-type [req]
  (assoc-in req [:headers "Content-Type"] "application/json; charset=utf-8"))

(defn with-body [req]
  (cond-> req (:body-fn req) (assoc :body ((:body-fn req)))))

(defn with-json-body [req]
  (cond-> req (:body req) (assoc :body (json/write-str (:body req)))))

(defn strip-internal [req]
  (dissoc req :cfg :path-template :path :body-fn :token))

;; ─── Response middleware (resp -> resp) ───

(defn assert-ok [resp]
  (if (:error resp)
    (throw (ex-info "Telegram HTTP request failed" resp))
    resp))

(defn parse-json-body [resp]
  (cond-> resp (string? (:body resp)) (assoc :body (try-parse-json (:body resp)))))

(defn assert-tg-ok [resp]
  (let [body (:body resp)]
    (if (and (map? body) (false? (:ok body)))
      (throw (ex-info "Telegram API request failed"
                      {:error-code (:error_code body)
                       :description (:description body)
                       :parameters (:parameters body)
                       :status (:status resp)}))
      resp)))

(defn unwrap-result [resp]
  (cond-> resp
    (and (map? (:body resp)) (contains? (:body resp) :result))
    (assoc :body (:result (:body resp)))))

;; ─── Execution ───

(defn execute
  "Thread a request spec through request middleware into http-kit, then
  thread the response through response middleware. cfg is injected by
  with-cfg and read by subsequent middleware from the req map."
  [spec cfg]
  (-> ((with-cfg cfg) spec)
      with-path
      with-url
      with-content-type
      with-body
      with-json-body
      strip-internal
      http/request
      deref
      assert-ok
      parse-json-body
      assert-tg-ok
      unwrap-result))

;; ─── Client factory ───

(defn create-client
  "Create a Telegram HTTP client backed by the Telegram Bot API.
  Opts may override :base-url, :token and :chat-id (token defaults from
  config). :chat-id is intended to be set here and used as the default
  chat for every call, but any method may override it via :chat-id in its
  opts. Calls getMe on creation and throws if the token is invalid or the
  server is not reachable."
  ([] (create-client {}))
  ([opts]
   (let [initial-cfg (merge {:base-url default-base-url
                             :token    (conf/read-value :telegram :token)}
                            opts)
         cfg-atom (atom initial-cfg)]
     (get-me! (:base-url initial-cfg) (:token initial-cfg))
     (reify TelegramClient
       ;; ─── Config management ───
       (get-config [_] @cfg-atom)
       (set-config! [_ cfg] (reset! cfg-atom cfg))

        ;; ─── Sending messages ───
       (send-message [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendMessage"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-photo [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendPhoto"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-video [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendVideo"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-animation [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendAnimation"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-audio [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendAudio"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-poll [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendPoll"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-chat-action [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/sendChatAction"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (get-chat [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-chat cfg opts)]
           (execute {:method :post :path-template "/getChat"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))))))

(defn send-posts! [{:keys [posts chat-id]}]
  (run!
   (fn [post]
     (let [client (create-client {:chat-id chat-id})
           section (cond
                     (= (:content-type-id post) 1)
                     (str "🎬 <b>" (:feed-title post) " — " (:title post) "</b>\n\n")
                     (= (:content-type-id post) 2)
                     (str "🎙 <b>" (:feed-title post) " — " (:title post) "</b>\n\n")
                     (= (:content-type-id post) 3)
                     (str "📰 <b>" (:feed-title post) " — " (:title post) "</b>\n\n"))

           verb (cond
                  (= (:content-type-id post) 1)
                  "Watch"
                  (= (:content-type-id post) 2)
                  "Listen"
                  (= (:content-type-id post) 3)
                  "Read")

           reply-markup (when (or (:url post) (:stream-url post))
                          {:inline_keyboard [[{:text verb
                                               :url
                                               (or (:url post) (:stream-url post))}]]})

           message (cond
                     (= (:content-type-id post) 1)
                     (str section
                          (:stream-url post))
                     (= (:content-type-id post) 2)
                     (str section
                          (or (:url post)
                              (:stream-url post)))
                     (= (:content-type-id post) 3)
                     (str section
                          (or (:url post)
                              (:stream-url post))))]

       (if (util/unfurlable? (or (:url post) (:stream-url post)))
         (send-message client {:chat-id chat-id
                               :text message
                               :parse-mode "HTML"
                               :disable-web-page-preview false})
         (send-photo client {:chat-id chat-id
                             :photo (:thumbnail post)
                             :caption (str section (util/truncate (util/strip-tags (or (:info post) " ")) 600) "\n")
                             :parse-mode "HTML"
                             :reply-markup reply-markup}))))
   posts))

(comment
  (def client (create-client {:chat-id "-5073615757"}))

  (def ds (db.util/conn))
  (def bundle-id 26)

  (send-posts! {:posts (-> (bundles/get-outgoing-posts ds {:bundle-id bundle-id
                                                           :seed (util/get-utc-timestamp-string)
                                                           :limit 3})
                           (:data))
                :chat-id "-5073615757"})

  ;; chat-id from the client
  (send-message client {:text "Hello from Clojure!"
                        :parse-mode "HTML"})

  ;; override chat-id for a single call
  (send-message client {:chat-id "123456789"
                        :text "<b>Bold</b> message"
                        :parse-mode "HTML"
                        :link-preview-options {:is-disabled true}})

  (send-photo client {:chat-id "123456789"
                      :photo "https://picsum.photos/1024"
                      :caption "Look at this"
                      :parse-mode "HTML"})

  (send-video client {:chat-id "123456789"
                      :video "https://example.com/video.mp4"
                      :caption "Check this out"})

  (send-animation client {:chat-id "123456789"
                          :animation "https://example.com/animation.gif"})

  (send-audio client {:chat-id "123456789"
                      :audio "https://example.com/audio.mp3"
                      :caption "Listen to this"})

  (send-poll client {:chat-id "123456789"
                     :question "How are you?"
                     :options [{:text "Awesome!"} {:text "Good!"} {:text "Not bad!"}]
                     :allows-multiple-answers false})

  (send-chat-action client {:chat-id "123456789" :action "typing"})

  (send-message client {:chat-id "123456789"
                        :text "Replying..."
                        :reply-parameters {:message-id 10}})

  (send-message client {:chat-id "123456789"
                        :text "With inline keyboard"
                        :reply-markup {:inline_keyboard
                                       [[{:text "Open"
                                          :url "https://example.com"}]]}})

  ;; override the configured token for a single call via :token
  (send-message client {:text "Sent with another bot's token"
                        :token "another-bot-token"})

  ;; inspect / replace the server config at runtime
  (get-config client)
  (set-config! client {:base-url "https://api.telegram.org"
                       :token "another-bot-token"
                       :chat-id "123456789"})

  ;; create a second client pointing at a different bot
  (def staging (create-client {:token "staging-bot-token"
                               :chat-id "123456789"}))
  (send-message staging {:text "Sent via staging!"})

  ;; or call execute directly with a one-off cfg to override the token
  ;; without creating a new client
  (execute {:method :post
            :path-template "/sendMessage"
            :body-fn (body-from-opts {:chat-id "123456789" :text "hi"})}
           {:base-url "https://api.telegram.org"
            :token "one-off-token"})

  ;; or thread the middleware yourself, swapping in a different cfg via
  ;; with-cfg — this is all execute does internally, so you can insert
  ;; or replace any middleware step when you need finer control
  (-> {:method :post
       :path-template "/sendMessage"
       :body-fn (body-from-opts {:chat-id "123456789" :text "hi"})}
      ((with-cfg {:base-url "https://api.telegram.org"
                  :token "one-off-token"}))
      with-path with-url with-content-type
      with-body with-json-body strip-internal
      http/request deref assert-ok parse-json-body assert-tg-ok unwrap-result)
  ())
