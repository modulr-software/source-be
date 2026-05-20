 (ns source.routes.integration-channels
   (:require [ring.util.response :as res]
             [source.routes.openapi :as api]
             [source.jobs.core :as jobs]
             [source.jobs.handlers :as handlers]
             [source.util :as util]
             [congest.jobs :as congest]
             [source.db.honey :as hon]
             [source.workers.schemas :as schemas]))

(defn channels
  {:summary "Get all channels for the integration ID"
   :parameters (api/params
                :path [:map api/IntegrationIdParam])
   :responses {200 {:body schemas/IntegrationChannels}}}

  [{:keys [ds path-params] :as _request}]
  (res/response
   (hon/find ds {:tname :integration-channels
                 :where [:= :bundle-id (:id path-params)]})))

(defn channel
  {:summary "Get a channel by ID"
   :parameters (api/params
                :path [:map
                       api/IntegrationIdParam
                       api/IntegrationChannelIdParam])
   :responses {200 {:body schemas/IntegrationChannel}
               404 {:body (api/response-schema)}}}

  [{:keys [ds path-params] :as _request}]
  (let [channel (hon/find-one ds {:tname :integration-channels
                                  :where [:= :id (:channel-id path-params)]})]
    (if (some? channel)
      (res/response channel)
      (res/response {:message "The integration channel with the given ID does not exist."}))))

(defn create-channel
  {:summary "Create a channel"
   :parameters (api/params
                :path [:map api/IntegrationIdParam]
                :body api/IntegrationChannelParams)
   :responses {200 {:body schemas/IntegrationChannel}
               400 {:body (api/response-schema)}}}

  [{:keys [ds js body path-params] :as _request}]
  (let [{:keys [platform channel-id thread-id post-interval]} body
        ;;TODO: validate input data
        {:keys [id] :as channel} (hon/insert! ds {:tname :integration-channels
                                                  :data {:name platform
                                                         :bundle-id (:id path-params)
                                                         :channel-id channel-id
                                                         :thread-id thread-id
                                                         :post-interval post-interval}
                                                  :ret :1})]

    (->> (jobs/prepare-congest-metadata
          ds
          {:id (handlers/integration-channel-job-id id (:id path-params))
           :initial-delay 0
           :auto-start true
           :stop-after-fail false
           :interval post-interval
           :recurring? true
           :args {:channel-id channel-id
                  :platform platform
                  :bundle-id (:id path-params)}
           :handler :post-to-integration-channel
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))

    (res/response channel)))

#_(defn update-channel
    {:summary "Update a channel by ID"
     :parameters {:path api/IntegrationIdParam
                  :body api/IntegrationChannelParams}
     :responses {200 {:Body (api/response-schema)}
                 400 {:body (api/response-schema)}}}

    [{:keys [ds bundle-id path-params body] :as _request}]
  ;; TODO: implement channel update
    (res/response {:message "not implemented"}))

(defn delete-channel
  {:summary "Delete a channel by ID"
   :parameters (api/params
                :path [:map
                       api/IntegrationIdParam
                       api/IntegrationChannelIdParam])
   :responses {200 {:body (api/response-schema)}
               403 {:body (api/response-schema)}}}

  [{:keys [ds js path-params] :as _request}]
  (hon/delete! ds {:tname :integration-channels
                   :where [:= :id (:channel-id path-params)]})
  (->> (:id path-params)
       (handlers/integration-channel-job-id (:channel-id path-params))
       (congest/deregister! js))
  (res/response {:message "successfully deleted channel"}))
