(ns source.workers.integration-channels.whatsapp-channel.interface
  (:require [source.workers.integration-channels.interface :as intchan]
            [source.workers.whatsapp :as wa]))

(defn- -send-posts! [*config posts]
  (when (:valid @*config)
    (wa/send-posts! {:posts posts
                     :group-id (:channel-id @*config)})))

(defn- -handshake [*config]
  (if (not (:valid @*config))
    (let [{:keys [channel-id phone-number]} @*config
          group-id (wa/group-id-by-name channel-id)]
      (if (wa/group-participant? group-id phone-number)
        (do
          (swap! *config assoc :channel-id group-id)
          (swap! *config assoc :valid true)
          true)
        (throw (ex-info "phone number does not belong to group" {:causes #{:invalid-group-name
                                                                           :invalid-phone-number}}))))
    true))

(defn create-client
  [{:keys [valid token channel-id phone-number]}]
  (let [*config (atom {:valid (or valid false)
                       :token token
                       :channel-id channel-id
                       :phone-number phone-number})]
    (reify intchan/IntegrationChannel
      (view-config [_]
        @*config)
      (handshake [_]
        (-handshake *config))
      (send-posts! [_ posts]
        (-send-posts! *config posts)))))

(comment
  (require '[source.workers.bundles :as bundles])
  (require '[source.util :as util])
  (require '[source.db.util :as db.util])

  (def ds (db.util/conn))
  (def bundle-id 26)
  (def group-id (wa/group-id-by-name "thisisatest"))

  ;; valid — phone-number is a member of the group
  (def channel (create-client {:chat-id      group-id
                               :phone-number "27607205781"
                               :valid        false}))

  ;; invalid — throws "phone number is not a member of the WhatsApp group"
  (create-client {:chat-id      group-id
                  :phone-number "00000000000"
                  :valid        false})

  ;; send a batch of posts through the channel — :chat-id overrides the instance's
  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed     (util/get-utc-timestamp-string)
                                        :limit    1}))
                        :chat-id group-id})

  ;; same call, omitting :chat-id — falls back to the chat-id captured at create-client
  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed     (util/get-utc-timestamp-string)
                                        :limit    3}))})
  ())
