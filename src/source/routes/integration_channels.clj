 (ns source.routes.integration-channels
   (:require [ring.util.response :as res]
             [source.routes.openapi :as api]
             [source.jobs.core :as jobs]
             [source.jobs.handlers :as handlers]
             [source.util :as util]
             [congest.jobs :as congest]
             [source.db.honey :as hon]))

(defn create-channel
  {:summary "Create a channel"
   :parameters (api/params :body api/IntegrationChannelParams)
   :responses {200 {:body (api/response-schema)}
               400 {:body (api/response-schema)}}}

  [{:keys [ds js bundle-id body] :as _request}]
  (let [{:keys [platform channel-id thread-id post-interval]} body
        ;;TODO: validate input data
        {:keys [id]} (hon/insert! ds {:tname :integration-channels
                                      :data {:name platform
                                             :channel-id channel-id
                                             :thread-id thread-id
                                             :post-interval post-interval}
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
                  :bundle-id bundle-id}
           :handler :post-to-integration-channel
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js)))

  (res/response {:message "successfully added channel"}))

(defn channel
  {:summary "Get a channel by ID"
   :parameters {:path api/IntegrationIdParam}
   :responses {200 {:body (api/response-schema)}
               404 {:body (api/response-schema)}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  ;; TODO: implement channel retrieval
  (res/response {:message "not implemented"}))

(defn update-channel
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
   :parameters {:path api/IntegrationIdParam}
   :responses {200 {:body (api/response-schema)}
               403 {:body (api/response-schema)}}}

  [{:keys [ds js bundle-id path-params] :as _request}]
  (hon/delete! ds {:tname :integration-channels
                   :where [:= :id (:id path-params)]})
  (->> bundle-id
       (handlers/integration-channel-job-id (:id path-params))
       (congest/deregister! js))
  (res/response {:message "successfully deleted channel"}))
