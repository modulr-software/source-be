(ns source.workers.integration-channels.telegram-channel.interface
  (:require [source.workers.integration-channels.interface :as intchan]
            [source.workers.telegram-client :as tg]))

(defn- -send-posts! [*config posts]
  (when (:valid @*config)
    (tg/send-posts! {:posts posts
                     :chat-id (:chat-id @*config)})))

(defn- -handshake [*config]
  (if (not (:valid @*config))
    (let [{:keys [chat-id token]} @*config
          client (tg/create-client {:chat-id chat-id :token token})]
      (tg/get-chat client {})
      (swap! *config assoc :valid true)
      true)
    true))

(defn create-client
  [{:keys [valid token chat-id]}]
  (let [*config (atom {:valid (or valid false)
                       :token token
                       :chat-id chat-id})]
    (reify intchan/IntegrationChannel
      (view-config [_]
        @*config)
      (handshake [_]
        (-handshake *config))
      (send-posts! [_ opts]
        (-send-posts! *config opts)))))

(comment
  (require '[source.workers.bundles :as bundles])
  (require '[source.util :as util])
  (require '[source.db.util :as db.util])

  (def ds (db.util/conn))
  (def bundle-id 26)
  (def chat-id "-5073615757")

  (def channel (create-client {:valid false :chat-id chat-id}))

  (intchan/handshake channel)        ; validates chat-id, sets :valid true
  (intchan/view-config channel)      ; {:valid true :chat-id ...}

  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed (util/get-utc-timestamp-string)
                                        :limit 1}))})

  ;; override chat-id for a single batch
  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed (util/get-utc-timestamp-string)
                                        :limit 1}))
                        :chat-id chat-id})
  ())
