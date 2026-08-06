(ns source.workers.integration-channels.slack-channel.interface
  (:require [source.workers.integration-channels.interface :as intchan]
            [source.workers.slack-client :as slack]))

(defn- -send-posts! [*config {:keys [posts chat-id token]}]
  (when (:valid @*config)
    (slack/send-posts! {:posts posts
                        :channel-id (or chat-id (:chat-id @*config))
                        :token (or token (:token @*config))})))

(defn create-client
  [{:keys [valid token chat-id]}]
  (let [*config (atom {:valid (or valid false)
                       :token token
                       :chat-id chat-id})]
    (reify intchan/IntegrationChannel
      (view-config [_]
        @*config)
      (handshake [_]
        true)
      (send-posts! [_ opts]
        (-send-posts! *config opts)))))

(comment
  (require '[source.workers.bundles :as bundles])
  (require '[source.util :as util])
  (require '[source.db.util :as db.util])
  (require '[source.config :as conf])

  (def ds (db.util/conn))
  (def bundle-id 26)

  (def channel (create-client {:valid true
                               :chat-id "C0B6ZBDFV08"
                               :token (conf/read-value :slack :test-token)}))

  (intchan/handshake channel)      ; => true (no slack handshake yet)
  (intchan/view-config channel)

  (intchan/send-posts! channel
                       {:posts (:data (bundles/get-outgoing-posts
                                       ds
                                       {:bundle-id bundle-id
                                        :seed (util/get-utc-timestamp-string)
                                        :limit 1}))})
  ())
