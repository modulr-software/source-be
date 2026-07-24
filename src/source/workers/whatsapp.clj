(ns source.workers.whatsapp
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [source.config :as conf]))

(def ^:private default-base-url "https://whatsapp.modulrza.app")

(defprotocol WhatsAppClient
  "A WhatsApp HTTP client backed by a server speaking the WAHA HTTP API.

  All opts maps use kebab-case keys (e.g. :chat-id, :reply-to, :link-preview);
  they are converted to WAHA's mixed camelCase/snake_case format before the
  request is sent. :session may be omitted on any call to fall back to the
  session configured on the client.

  The server config (:base-url, :api-key, :session) is held in an atom and
  can be inspected or replaced at runtime via get-config / set-config!."

  ;; ─── Config management ───
  (get-config [this]
    "Return the current server config map (:base-url, :api-key, :session).")
  (set-config! [this cfg]
    "Replace the entire server config map. cfg: :base-url, :api-key, :session.")

  ;; ─── Sending messages ───
  (send-text [this opts]
    "Send a text message. opts: :chat-id, :text, :reply-to, :link-preview,
    :link-preview-high-quality, :id, :session")
  (send-image [this opts]
    "Send an image (RemoteFile or BinaryFile). opts: :chat-id, :file, :caption,
    :reply-to, :session")
  (send-video [this opts]
    "Send a video. opts: :chat-id, :file, :caption, :as-note, :convert,
    :reply-to, :session")
  (send-voice [this opts]
    "Send a voice message. opts: :chat-id, :file, :convert, :reply-to, :session")
  (send-file [this opts]
    "Send a file. opts: :chat-id, :file, :caption, :reply-to, :session")
  (send-location [this opts]
    "Send a location. opts: :chat-id, :latitude, :longitude, :title, :reply-to,
    :id, :session")
  (send-link-preview [this opts]
    "Send a link preview. opts: :chat-id, :url, :title, :id, :session")
  (send-poll [this opts]
    "Send a poll. opts: :chat-id, :poll {:name :options :multiple-answers},
    :reply-to, :id, :session")
  (send-buttons [this opts]
    "Send an interactive buttons message. opts: :chat-id, :header, :body,
    :footer, :buttons, :header-image, :session")
  (send-list [this opts]
    "Send an interactive list message. opts: :chat-id, :message, :reply-to,
    :session")
  (send-contact-vcard [this opts]
    "Send contact vCards. opts: :chat-id, :contacts, :reply-to, :id, :session")
  (forward-message [this opts]
    "Forward a message. opts: :chat-id, :message-id, :id, :session")
  (react [this opts]
    "React to a message. Send empty :reaction to remove. opts: :message-id,
    :reaction, :session")

  ;; ─── Group management ───
  (get-groups [this opts]
    "List all groups. opts: :sort-by, :sort-order, :limit, :offset, :exclude,
    :session")
  (get-group [this opts]
    "Get a single group. opts: :group-id, :session")
  (create-group [this opts]
    "Create a group. opts: :name, :participants (vector of ids or {:id ...}),
    :session")
  (delete-group [this opts]
    "Delete a group. opts: :group-id, :session")
  (get-groups-count [this opts]
    "Get the number of groups. opts: :session")
  (get-participants [this opts]
    "Get the participants of a group. opts: :group-id, :session")
  (add-participants [this opts]
    "Add participants to a group. opts: :group-id, :participants, :session")
  (remove-participants [this opts]
    "Remove participants from a group. opts: :group-id, :participants, :session")
  (promote-admins [this opts]
    "Promote participants to admins. opts: :group-id, :participants, :session")
  (demote-admins [this opts]
    "Demote admins to regular participants. opts: :group-id, :participants,
    :session")
  (set-subject [this opts]
    "Update the group subject (name). opts: :group-id, :subject, :session")
  (set-description [this opts]
    "Update the group description. opts: :group-id, :description, :session")
  (get-group-picture [this opts]
    "Get the group picture. opts: :group-id, :refresh, :session")
  (set-group-picture [this opts]
    "Set the group picture (RemoteFile or BinaryFile). opts: :group-id, :file,
    :session")
  (delete-group-picture [this opts]
    "Delete the group picture. opts: :group-id, :session")
  (get-invite-code [this opts]
    "Get the group invite code. opts: :group-id, :session")
  (revoke-invite-code [this opts]
    "Revoke and regenerate the group invite code. opts: :group-id, :session")
  (leave-group [this opts]
    "Leave a group. opts: :group-id, :session")
  (join-group [this opts]
    "Join a group via invite code or url. opts: :code, :session")
  (get-join-info [this opts]
    "Get info about a group before joining. opts: :code, :session")
  (refresh-groups [this opts]
    "Refresh groups from the server. opts: :session")
  (get-info-admin-only [this opts]
    "Get whether only admins can edit group info. opts: :group-id, :session")
  (set-info-admin-only [this opts]
    "Set whether only admins can edit group info. opts: :group-id,
    :admins-only, :session")
  (get-messages-admin-only [this opts]
    "Get whether only admins can send messages. opts: :group-id, :session")
  (set-messages-admin-only [this opts]
    "Set whether only admins can send messages. opts: :group-id,
    :admins-only, :session"))

;; ─── Key conversion (kebab-case -> WAHA mixed case) ───

(def ^:private key-overrides
  "WAHA mixes camelCase and snake_case; most keys convert via kebab->camel,
  these are the exceptions that need an explicit target string."
  {:reply-to "reply_to"})

(defn- convert-key [k]
  (if-some [override (key-overrides k)]
    (keyword override)
    (-> k name (str/replace #"-([a-z])" (fn [[_ c]] (str/upper-case c))) keyword)))

(defn- to-waha
  "Recursively converts kebab-case keys in maps/vectors to WAHA's key format."
  [x]
  (cond
    (map? x)    (reduce-kv (fn [m k v] (assoc m (convert-key k) (to-waha v))) {} x)
    (vector? x) (mapv to-waha x)
    :else       x))

;; ─── Helpers ───

(defn- participants [p]
  (mapv (fn [x] (if (map? x) x {:id x})) p))

(defn- try-parse-json [s]
  (try (json/read-str s :key-fn keyword) (catch Exception _ s)))

(defn- body-from-opts [opts]
  (fn [session] (to-waha (assoc opts :session session))))

(defn- ping!
  "GET {base-url}/ping to verify the server is reachable. Throws if the
  request errors or returns a non-2xx status."
  [base-url]
  (let [resp @(http/request {:method :get
                             :url (str base-url "/ping")
                             :timeout 5000})]
    (when (or (:error resp)
              (not (<= 200 (:status resp) 299)))
      (throw (ex-info "WhatsApp server is not reachable"
                      {:base-url base-url
                       :status (:status resp)
                       :error (:error resp)})))))

;; ─── Request middleware (req -> req) ───

(defn with-cfg [cfg]
  (fn [req] (assoc req :cfg cfg)))

(defn with-session [req]
  (assoc req :session (or (:session req) (-> req :cfg :session))))

(defn with-path [req]
  (assoc req :path (str/replace (:path-template req) #"\{session\}" (:session req))))

(defn with-url [req]
  (assoc req :url (str (-> req :cfg :base-url) (:path req))))

(defn with-auth [req]
  (assoc-in req [:headers "X-Api-Key"] (-> req :cfg :api-key)))

(defn with-content-type [req]
  (assoc-in req [:headers "Content-Type"] "application/json; charset=utf-8"))

(defn with-body [req]
  (cond-> req (:body-fn req) (assoc :body ((:body-fn req) (:session req)))))

(defn with-query [req]
  (cond-> req (:query-fn req) (assoc :query ((:query-fn req) (:session req)))))

(defn with-json-body [req]
  (cond-> req (:body req) (assoc :body (json/write-str (:body req)))))

(defn with-query-params [req]
  (cond-> req (:query req) (assoc :query-params (:query req))))

(defn strip-internal [req]
  (dissoc req :cfg :path-template :path :query :session :body-fn :query-fn))

;; ─── Response middleware (resp -> resp) ───

(defn assert-ok [resp]
  (if (:error resp)
    (throw (ex-info "WhatsApp HTTP request failed" resp))
    resp))

(defn parse-json-body [resp]
  (cond-> resp (string? (:body resp)) (assoc :body (try-parse-json (:body resp)))))

;; ─── Execution ───

(defn execute
  "Thread a request spec through request middleware into http-kit, then
  thread the response through response middleware. cfg is injected by
  with-cfg and read by subsequent middleware from the req map."
  [spec cfg]
  (-> ((with-cfg cfg) spec)
      with-session
      with-path
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
      assert-ok
      parse-json-body))

;; ─── Client factory ───

(defn create-client
  "Create a WhatsApp HTTP client backed by the WAHA API.
  Opts may override :base-url, :api-key and :session (defaults from config).
  Pings the server on creation and throws if it is not reachable."
  ([] (create-client {}))
  ([opts]
   (let [initial-cfg (merge {:base-url default-base-url
                             :api-key  (conf/read-value :whatsapp :token)
                             :session  (conf/read-value :whatsapp :session)}
                            opts)
         cfg-atom (atom initial-cfg)]
     (ping! (:base-url initial-cfg))
     (reify WhatsAppClient
       ;; ─── Config management ───
       (get-config [_] @cfg-atom)
       (set-config! [_ cfg] (reset! cfg-atom cfg))

       ;; ─── Sending messages ───
       (send-text [_ opts]
         (execute {:method :post :path-template "/api/sendText"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-image [_ opts]
         (execute {:method :post :path-template "/api/sendImage"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-video [_ opts]
         (execute {:method :post :path-template "/api/sendVideo"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-voice [_ opts]
         (execute {:method :post :path-template "/api/sendVoice"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-file [_ opts]
         (execute {:method :post :path-template "/api/sendFile"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-location [_ opts]
         (execute {:method :post :path-template "/api/sendLocation"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-link-preview [_ opts]
         (execute {:method :post :path-template "/api/sendLinkPreview"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-poll [_ opts]
         (execute {:method :post :path-template "/api/sendPoll"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-buttons [_ opts]
         (execute {:method :post :path-template "/api/sendButtons"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-list [_ opts]
         (execute {:method :post :path-template "/api/sendList"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (send-contact-vcard [_ opts]
         (execute {:method :post :path-template "/api/sendContactVcard"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (forward-message [_ opts]
         (execute {:method :post :path-template "/api/forwardMessage"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))
       (react [_ opts]
         (execute {:method :post :path-template "/api/reaction"
                   :session (:session opts) :body-fn (body-from-opts opts)} @cfg-atom))

       ;; ─── Group management ───
       (get-groups [_ opts]
         (execute {:method :get :path-template "/api/{session}/groups"
                   :session (:session opts)
                   :query-fn (fn [_] (to-waha (dissoc opts :session)))} @cfg-atom))
       (get-group [_ opts]
         (execute {:method :get
                   :path-template (str "/api/{session}/groups/" (:group-id opts))
                   :session (:session opts)} @cfg-atom))
       (create-group [_ opts]
         (execute {:method :post :path-template "/api/{session}/groups"
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:name (:name opts)
                                              :participants (participants (:participants opts))}))} @cfg-atom))
       (delete-group [_ opts]
         (execute {:method :delete
                   :path-template (str "/api/{session}/groups/" (:group-id opts))
                   :session (:session opts)} @cfg-atom))
       (get-groups-count [_ opts]
         (execute {:method :get :path-template "/api/{session}/groups/count"
                   :session (:session opts)} @cfg-atom))
       (get-participants [_ opts]
         (execute {:method :get
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/participants")
                   :session (:session opts)} @cfg-atom))
       (add-participants [_ opts]
         (execute {:method :post
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/participants/add")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:participants (participants (:participants opts))}))} @cfg-atom))
       (remove-participants [_ opts]
         (execute {:method :post
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/participants/remove")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:participants (participants (:participants opts))}))} @cfg-atom))
       (promote-admins [_ opts]
         (execute {:method :post
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/admin/promote")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:participants (participants (:participants opts))}))} @cfg-atom))
       (demote-admins [_ opts]
         (execute {:method :post
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/admin/demote")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:participants (participants (:participants opts))}))} @cfg-atom))
       (set-subject [_ opts]
         (execute {:method :put
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/subject")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:subject (:subject opts)}))} @cfg-atom))
       (set-description [_ opts]
         (execute {:method :put
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/description")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:description (:description opts)}))} @cfg-atom))
       (get-group-picture [_ opts]
         (execute {:method :get
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/picture")
                   :session (:session opts)
                   :query-fn (fn [_] (to-waha (dissoc opts :session :group-id)))} @cfg-atom))
       (set-group-picture [_ opts]
         (execute {:method :put
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/picture")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:file (:file opts)}))} @cfg-atom))
       (delete-group-picture [_ opts]
         (execute {:method :delete
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/picture")
                   :session (:session opts)} @cfg-atom))
       (get-invite-code [_ opts]
         (execute {:method :get
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/invite-code")
                   :session (:session opts)} @cfg-atom))
       (revoke-invite-code [_ opts]
         (execute {:method :post
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/invite-code/revoke")
                   :session (:session opts)} @cfg-atom))
       (leave-group [_ opts]
         (execute {:method :post
                   :path-template (str "/api/{session}/groups/" (:group-id opts) "/leave")
                   :session (:session opts)} @cfg-atom))
       (join-group [_ opts]
         (execute {:method :post :path-template "/api/{session}/groups/join"
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:code (:code opts)}))} @cfg-atom))
       (get-join-info [_ opts]
         (execute {:method :get :path-template "/api/{session}/groups/join-info"
                   :session (:session opts)
                   :query-fn (fn [_] (to-waha {:code (:code opts)}))} @cfg-atom))
       (refresh-groups [_ opts]
         (execute {:method :post :path-template "/api/{session}/groups/refresh"
                   :session (:session opts)} @cfg-atom))
       (get-info-admin-only [_ opts]
         (execute {:method :get
                   :path-template (str "/api/{session}/groups/" (:group-id opts)
                                       "/settings/security/info-admin-only")
                   :session (:session opts)} @cfg-atom))
       (set-info-admin-only [_ opts]
         (execute {:method :put
                   :path-template (str "/api/{session}/groups/" (:group-id opts)
                                       "/settings/security/info-admin-only")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:admins-only (:admins-only opts)}))} @cfg-atom))
       (get-messages-admin-only [_ opts]
         (execute {:method :get
                   :path-template (str "/api/{session}/groups/" (:group-id opts)
                                       "/settings/security/messages-admin-only")
                   :session (:session opts)} @cfg-atom))
       (set-messages-admin-only [_ opts]
         (execute {:method :put
                   :path-template (str "/api/{session}/groups/" (:group-id opts)
                                       "/settings/security/messages-admin-only")
                   :session (:session opts)
                   :body-fn (fn [_] (to-waha {:admins-only (:admins-only opts)}))} @cfg-atom))))))

(comment
  (send-text client {:chat-id "11111111111@c.us" :text "Replying..."
                     :reply-to "false_11111111111@c.us_AAAA"})
  (send-image client {:chat-id "11111111111@c.us"
                      :file {:mimetype "image/jpeg" :url "https://picsum.photos/1024"}
                      :caption "Look at this"})
  (send-poll client {:chat-id "11111111111@c.us"
                     :poll {:name "How are you?"
                            :options ["Awesome!" "Good!" "Not bad!"]
                            :multiple-answers false}})
  (get-groups client nil)
  (get-groups client {:sort-by "subject" :sort-order "asc" :limit 10})
  (create-group client {:name "My Group" :participants ["11111111111@c.us"]})
  (get-participants client {:group-id "123123123@g.us"})
  (add-participants client {:group-id "123123123@g.us"
                            :participants ["11111111111@c.us"]})
  (set-subject client {:group-id "123123123@g.us" :subject "New Name"})
  (get-invite-code client {:group-id "123123123@g.us"})
  (join-group client {:code "https://chat.whatsapp.com/1234567890abcdef"})
  (set-messages-admin-only client {:group-id "123123123@g.us" :admins-only true})

  (get-groups client {:sort-by "subject" :session "other"})

  (def client (create-client))
  (send-text client {:chat-id "27607205781@c.us" :text "Hello from Clojure!"})

  ;; override the configured session for a single call via :session
  (send-text client {:chat-id "11111111111@c.us" :text "Sent from another session!"
                     :session "other"})

  ;; inspect / replace the server config at runtime
  (get-config client)
  (set-config! client {:base-url "https://staging.whatsapp.example"
                       :api-key "staging-api-key"
                       :session "staging-session"})

  ;; create a second client pointing at a different WAHA instance / api-key
  (def staging (create-client {:base-url "https://staging.whatsapp.example"
                               :api-key "staging-api-key"
                               :session "staging-session"}))
  (send-text staging {:chat-id "11111111111@c.us" :text "Sent via staging!"})

  ;; or call execute directly with a one-off cfg to override base-url / api-key
  ;; without creating a new client
  (execute {:method :post
            :path-template "/api/sendText"
            :session "default"
            :body-fn (body-from-opts {:chat-id "11111111111@c.us" :text "hi"})}
           {:base-url "https://other.whatsapp.example"
            :api-key "one-off-token"
            :session "default"})

  ;; or thread the middleware yourself, swapping in a different cfg via
  ;; with-cfg — this is all execute does internally, so you can insert
  ;; or replace any middleware step when you need finer control
  (-> {:method :post
       :path-template "/api/sendText"
       :session "default"
       :body-fn (body-from-opts {:chat-id "11111111111@c.us" :text "hi"})}
      ((with-cfg {:base-url "https://other.whatsapp.example"
                  :api-key "one-off-token"
                  :session "default"}))
      with-session with-path with-url with-auth with-content-type
      with-body with-query with-json-body with-query-params strip-internal
      http/request deref assert-ok parse-json-body)
  ())
