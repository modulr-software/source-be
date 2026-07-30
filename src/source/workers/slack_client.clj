(ns source.workers.slack-client
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [source.config :as conf]
            [source.util :as util]))

(def ^:private default-base-url "https://slack.com")

(defprotocol SlackClient
  "A Slack HTTP client backed by the Slack Web API
  (https://slack.com/api).

  The client is configured with a :channel-id and :token at
  instantiation; both may be overridden on a per-call basis via the
  :channel-id / :token keys in opts. :unfurl-links defaults to true
  on the message-sending functions and may be overridden via opts
  (or, failing that, the client's configured :unfurl-links).

  All opts maps use kebab-case keys (e.g. :channel-id, :thread-ts,
  :unfurl-links); they are converted to Slack's snake_case format
  before the request is sent (:channel-id becomes :channel). :token
  may be omitted on any call to fall back to the token configured on
  the client (useful for multi-workspace apps that store a
  per-channel access token).

  The blocks / attachments fields accept native Clojure data
  (vectors / maps) and are sent as JSON structures in the body.

  The server config (:base-url, :token, :channel-id, :unfurl-links)
  is held in an atom and can be inspected or replaced at runtime via
  get-config / set-config!.

  Authentication uses an `Authorization: Bearer <token>` header on
  every request (Slack accepts this in addition to the `token`
  form/query parameter documented in the OpenAPI spec)."

  ;; ─── Config management ───
  (get-config [this]
    "Return the current server config map (:base-url, :token,
    :channel-id, :unfurl-links).")
  (set-config! [this cfg]
    "Replace the entire server config map. cfg: :base-url, :token,
    :channel-id, :unfurl-links.")

  ;; ─── Sending messages ───
  (send-message [this opts]
    "Send a message to a channel. opts: :channel-id, :text, :blocks,
    :attachments, :as-user, :icon-emoji, :icon-url, :link-names,
    :mrkdwn, :parse, :reply-broadcast, :thread-ts, :unfurl-links,
    :unfurl-media, :username, :token")
  (send-post [this opts]
    "Send a post (rich content) to a channel. Constructs the request
    body via build-post-body from :text and :action-text.
    :unfurl-links and :unfurl-media both default to true. opts:
    :channel-id, :text, :action-text, :blocks, :attachments, :as-user,
    :icon-emoji, :icon-url, :link-names, :mrkdwn, :parse,
    :reply-broadcast, :thread-ts, :unfurl-links, :unfurl-media,
    :username, :token")
  (update-message [this opts]
    "Update an existing message. opts: :channel-id, :ts, :text,
    :blocks, :attachments, :as-user, :link-names, :parse, :token")
  (delete-message [this opts]
    "Delete a message. opts: :channel-id, :ts, :as-user, :token")
  (schedule-message [this opts]
    "Schedule a message for future delivery. opts: :channel-id,
    :post-at, :text, :blocks, :attachments, :parse, :as-user,
    :link-names, :unfurl-links, :unfurl-media, :thread-ts,
    :reply-broadcast, :token")
  (schedule-post [this opts]
    "Schedule a post (rich content) for future delivery. Same as
    schedule-message; kept as a separate method to align with our
    use-cases. opts: :channel-id, :post-at, :text, :blocks,
    :attachments, :parse, :as-user, :link-names, :unfurl-links,
    :unfurl-media, :thread-ts, :reply-broadcast, :token")
  (list-scheduled-messages [this opts]
    "List scheduled messages. opts: :channel-id, :latest, :oldest,
    :limit, :cursor, :token")
  (delete-scheduled-message [this opts]
    "Delete a pending scheduled message. opts: :channel-id,
    :scheduled-message-id, :as-user, :token")
  (get-permalink [this opts]
    "Retrieve a permalink URL for a specific message. opts:
    :channel-id, :message-ts, :token"))

;; ─── Key conversion (kebab-case -> Slack snake_case) ───

(defn- convert-key [k]
  (-> k name (str/replace #"-" "_") keyword))

(defn- to-slack
  "Recursively converts kebab-case keys in maps/vectors to Slack's
  snake_case key format. Values are left untouched (blocks /
  attachments stay as native Clojure data and are serialized as
  JSON in the body)."
  [x]
  (cond
    (map? x)    (reduce-kv (fn [m k v] (assoc m (convert-key k) (to-slack v))) {} x)
    (vector? x) (mapv to-slack x)
    :else       x))

;; ─── Helpers ───

(defn- try-parse-json [s]
  (try (json/read-str s :key-fn keyword) (catch Exception _ s)))

(defn- body-from-opts [opts]
  (fn [] (to-slack opts)))

(defn- ping!
  "GET {base-url}/api/api.test to verify the Slack API is reachable.
  Throws if the request errors or returns a non-2xx status."
  [base-url]
  (let [resp @(http/request {:method :get
                             :url (str base-url "/api/api.test")
                             :timeout 5000})]
    (when (or (:error resp)
              (not (<= 200 (:status resp) 299)))
      (throw (ex-info "Slack API is not reachable"
                      {:base-url base-url
                       :status (:status resp)
                       :error (:error resp)})))))

(defn- resolve-channel
  "Resolve the channel for a request. opts may override the client's
  configured :channel-id via :channel-id; the resolved value is
  placed under :channel (Slack's expected field name) and the
  :channel-id key is removed from opts."
  [cfg opts]
  (let [channel-id (or (:channel-id opts) (:channel-id cfg))]
    (-> opts (dissoc :channel-id) (assoc :channel channel-id))))

(defn- with-default-unfurl
  "Default :unfurl-links to true unless explicitly provided in opts
  (or, failing that, the client's configured :unfurl-links)."
  [cfg opts]
  (assoc opts :unfurl-links
         (if (contains? opts :unfurl-links)
           (:unfurl-links opts)
           (get cfg :unfurl-links true))))

(defn- with-default-unfurl-media
  "Default :unfurl-media to true unless explicitly provided in opts
  (or, failing that, the client's configured :unfurl-media)."
  [cfg opts]
  (assoc opts :unfurl-media
         (if (contains? opts :unfurl-media)
           (:unfurl-media opts)
           (get cfg :unfurl-media true))))

(defn- build-post-body
  "Construct the request body for a Slack post. Receives the resolved
  opts (:channel, :title, :thumbnail, :text, :action-text, :url, :unfurl-links, :unfurl-media,
  ...) and returns the body map to be sent to chat.postMessage."
  [{:keys [title thumbnail text action-text url] :as opts}]
  (if (util/unfurlable? url)
    opts
    (merge opts {:blocks [{:type "section"
                           :text {:type "mrkdwn"
                                  :text text}
                           :accessory {:type "image"
                                       :image_url thumbnail
                                       :alt_text title}}
                          {:type "actions"
                           :elements [{:type "button"
                                       :text {:type "plain_text"
                                              :text action-text}
                                       :url url}]}]})))

;; ─── Request middleware (req -> req) ───

(defn with-cfg [cfg]
  (fn [req] (assoc req :cfg cfg)))

(defn with-url [req]
  (assoc req :url (str (-> req :cfg :base-url) (:path-template req))))

(defn with-auth [req]
  (assoc-in req [:headers "Authorization"]
            (str "Bearer " (or (:token req) (-> req :cfg :token)))))

(defn with-content-type [req]
  (assoc-in req [:headers "Content-Type"] "application/json; charset=utf-8"))

(defn with-body [req]
  (cond-> req (:body-fn req) (assoc :body ((:body-fn req)))))

(defn with-query [req]
  (cond-> req (:query-fn req) (assoc :query ((:query-fn req)))))

(defn with-json-body [req]
  (cond-> req (:body req) (assoc :body (json/write-str (:body req)))))

(defn with-query-params [req]
  (cond-> req (:query req) (assoc :query-params (:query req))))

(defn strip-internal [req]
  (dissoc req :cfg :path-template :token :body-fn :query-fn :query))

;; ─── Response middleware (resp -> resp) ───

(defn assert-ok [resp]
  (cond
    (:error resp)
    (throw (ex-info "Slack HTTP request failed" resp))

    (not (<= 200 (:status resp) 299))
    (throw (ex-info "Slack HTTP request failed" resp))

    (and (map? (:body resp)) (false? (:ok (:body resp))))
    (throw (ex-info (str "Slack API error: "
                         (or (:error (:body resp)) "unknown"))
                    resp))

    :else resp))

(defn parse-json-body [resp]
  (cond-> resp (string? (:body resp)) (assoc :body (try-parse-json (:body resp)))))

;; ─── Execution ───

(defn execute
  "Thread a request spec through request middleware into http-kit,
  then thread the response through response middleware. cfg is
  injected by with-cfg and read by subsequent middleware from the
  req map."
  [spec cfg]
  (-> ((with-cfg cfg) spec)
      with-url
      with-auth
      with-content-type
      with-body
      with-query
      with-json-body
      with-query-params
      strip-internal
      http/request
      deref
      parse-json-body
      assert-ok))

;; ─── Client factory ───

(defn create-client
  "Create a Slack HTTP client backed by the Slack Web API.
  Opts may override :base-url, :token, :channel-id and
  :unfurl-links (token defaults from config). :channel-id is
  intended to be set here and used as the default channel for every
  call, but any method may override it via :channel-id in its opts.
  Pings api.test on creation and throws if the API is not reachable."
  ([] (create-client {}))
  ([opts]
   (let [initial-cfg (merge {:base-url     default-base-url
                             :token        (conf/read-value :slack :token)
                             :unfurl-links true}
                            opts)
         cfg-atom (atom initial-cfg)]
     (ping! (:base-url initial-cfg))
     (reify SlackClient
       ;; ─── Config management ───
       (get-config [_] @cfg-atom)
       (set-config! [_ cfg] (reset! cfg-atom cfg))

       ;; ─── Sending messages ───
       (send-message [_ opts]
         (let [cfg @cfg-atom
               opts (->> opts (resolve-channel cfg) (with-default-unfurl cfg))]
           (execute {:method :post :path-template "/api/chat.postMessage"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (send-post [_ opts]
         (let [cfg @cfg-atom
               opts (->> opts
                         (resolve-channel cfg)
                         (with-default-unfurl cfg)
                         (with-default-unfurl-media cfg))
               body (build-post-body (dissoc opts :token))]
           (execute {:method :post :path-template "/api/chat.postMessage"
                     :token (:token opts)
                     :body-fn (body-from-opts body)} cfg)))
       (update-message
         {:dev/debt "we need to store message ids before this can be used"}
         [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-channel cfg opts)]
           (execute {:method :post :path-template "/api/chat.update"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (delete-message
         {:dev/debt "we need to store message ids before this can be used"}
         [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-channel cfg opts)]
           (execute {:method :post :path-template "/api/chat.delete"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (schedule-message [_ opts]
         (let [cfg @cfg-atom
               opts (->> opts (resolve-channel cfg) (with-default-unfurl cfg))]
           (execute {:method :post :path-template "/api/chat.scheduleMessage"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (schedule-post [_ opts]
         (let [cfg @cfg-atom
               opts (->> opts (resolve-channel cfg) (with-default-unfurl cfg))]
           (execute {:method :post :path-template "/api/chat.scheduleMessage"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (list-scheduled-messages [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-channel cfg opts)]
           (execute {:method :get :path-template "/api/chat.scheduledMessages.list"
                     :token (:token opts)
                     :query-fn (fn [] (to-slack (dissoc opts :token)))} cfg)))
       (delete-scheduled-message [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-channel cfg opts)]
           (execute {:method :post :path-template "/api/chat.deleteScheduledMessage"
                     :token (:token opts)
                     :body-fn (body-from-opts (dissoc opts :token))} cfg)))
       (get-permalink
         {:dev/debt "we need to store message ids before this can be used"}
         [_ opts]
         (let [cfg @cfg-atom
               opts (resolve-channel cfg opts)]
           (execute {:method :get :path-template "/api/chat.getPermalink"
                     :token (:token opts)
                     :query-fn (fn [] (to-slack (dissoc opts :token)))} cfg)))))))

(comment
  (def client (create-client {:token (conf/read-value :slack :test-token)
                              :channel-id "C0B6ZBDFV08"}))

  ;; channel-id from the client, unfurl-links defaults to true
  (send-message client {:text "Hello from Clojure!"})

  (send-post client {})

  ;; override channel-id and unfurl-links for a single call
  (send-message client {:text "Replying in a thread"
                        :thread-ts "1690000000.000000"
                        :reply-broadcast true
                        :unfurl-links false})

  (update-message client {:ts "1690000000.000000"
                          :text "Edited text"})

  (delete-message client {:ts "1690000000.000000"})

  (schedule-message client {:post-at "1690003600"
                            :text "Scheduled hello"})

  (schedule-post client {:post-at "1690003600"})

  (list-scheduled-messages client {:limit 10})

  (delete-scheduled-message client {:scheduled-message-id "Q1234567890"})

  (get-permalink client {:message-ts "1690000000.000000"})

  ;; override the configured token for a single call via :token
  (send-message client {:text "Sent with another workspace's token"
                        :token "xoxb-other-workspace-token"})

  ;; inspect / replace the server config at runtime
  (get-config client)
  (set-config! client {:base-url     "https://slack.com"
                       :token        "xoxb-staging-token"
                       :channel-id   "C1234567890"
                       :unfurl-links false})

  ;; create a second client pointing at a different token / channel
  (def staging (create-client {:token      "xoxb-staging-token"
                               :channel-id "C1234567890"}))
  (send-message staging {:text "Sent via staging!"})

  ;; or call execute directly with a one-off cfg to override base-url /
  ;; token without creating a new client
  (execute {:method :post
            :path-template "/api/chat.postMessage"
            :body-fn (body-from-opts {:channel "C1234567890" :text "hi"})}
           {:base-url "https://slack.com"
            :token "xoxb-one-off-token"})

  ;; or thread the middleware yourself, swapping in a different cfg via
  ;; with-cfg — this is all execute does internally, so you can insert
  ;; or replace any middleware step when you need finer control
  (-> {:method :post
       :path-template "/api/chat.postMessage"
       :body-fn (body-from-opts {:channel "C1234567890" :text "hi"})}
      ((with-cfg {:base-url "https://slack.com"
                  :token "xoxb-one-off-token"}))
      with-url with-auth with-content-type with-body
      with-query with-json-body with-query-params strip-internal
      http/request deref parse-json-body assert-ok)
  ())
