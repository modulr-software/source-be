(ns source.workers.integration-channels
  (:require [source.db.honey :as hon]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [source.util :as util]
            [congest.jobs :as congest]
            [source.workers.integration-channels.interface :as intchan]
            [source.workers.integration-channels.whatsapp-channel.interface :as wachan]
            [source.workers.integration-channels.slack-channel.interface :as slackchan]
            [source.workers.integration-channels.telegram-channel.interface :as telechan]
            [source.db.util :as db.util]
            [source.workers.whatsapp :as wa]
            [source.config :as conf]))

(defn create-channel!
  [ds js {:keys [platform bundle-id access-token channel-id thread-id post-interval posts]}]
  (let [{:keys [id] :as channel} (hon/insert! ds {:tname :integration-channels
                                                  :data {:name platform
                                                         :bundle-id bundle-id
                                                         :channel-id channel-id
                                                         :thread-id thread-id
                                                         :post-interval post-interval
                                                         :access-token access-token
                                                         :posts posts}
                                                  :ret :1})]
    (->> (jobs/prepare-congest-metadata
          ds
          {:id (handlers/integration-channel-job-id id bundle-id)
           :initial-delay 0
           :auto-start true
           :stop-after-fail false
           :interval post-interval
           :recurring? true
           :args {:channel-id channel-id
                  :platform platform
                  :bundle-id bundle-id
                  :posts posts}
           :handler :post-to-integration-channel
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))
    channel))

(comment
  (require '[source.workers.bundles :as bundles])

  (let [bundle-id 26
        ds (db.util/conn)
        channel (wachan/create-client {:valid false
                                       :channel-id "thisisatest"
                                       :phone-number "27607205781"})]
    (intchan/handshake channel) ; sets valid true and overwrites channel-id with real group-id
    (intchan/view-config channel) ; returns with channel-id overwritten with real group-id
    (intchan/send-posts! channel (:data (bundles/get-outgoing-posts
                                         ds
                                         {:bundle-id bundle-id
                                          :seed      (util/get-utc-timestamp-string)
                                          :limit     1}))))

  (let [bundle-id 26
        ds (db.util/conn)
        channel (slackchan/create-client {:valid false
                                          :chat-id "C0B6ZBDFV08"})]
    (intchan/send-posts! channel {:posts (:data (bundles/get-outgoing-posts
                                                 ds
                                                 {:bundle-id bundle-id
                                                  :seed     (util/get-utc-timestamp-string)
                                                  :limit    1}))
                                  :chat-id "C0B6ZBDFV08"
                                  :token (conf/read-value :slack :test-token)}))

  (let [bundle-id 26
        ds (db.util/conn)
        channel (telechan/create-client {:valid false
                                         :chat-id "-5073615757"})]
    (intchan/send-posts! channel {:posts (:data (bundles/get-outgoing-posts
                                                 ds
                                                 {:bundle-id bundle-id
                                                  :seed     (util/get-utc-timestamp-string)
                                                  :limit    1}))
                                  :chat-id "-5073615757"}))

  ())
