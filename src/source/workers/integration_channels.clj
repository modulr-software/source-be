(ns source.workers.integration-channels
  (:require [source.db.honey :as hon]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [source.util :as util]
            [congest.jobs :as congest]))

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
