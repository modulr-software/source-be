 (ns source.routes.integration-channels
   (:require [ring.util.response :as res]
             [source.routes.openapi :as api]))

(defn create-channel
  {:summary "Create a channel"
   :parameters {:path api/IntegrationIdParam
                :body api/IntegrationChannelParams}
   :responses {200 {:body (api/response-schema)}
               400 {:body (api/response-schema)}}}

  [{:keys [ds js bundle-id body path-params] :as _request}]
  ;; TODO: implement channel creation
  (res/response {:message "not implemented"}))

(defn channel
  {:summary "Get a channel by ID"
  :parameters {:path api/IntegrationChannelPathParam}
   :responses {200 {:body (api/response-schema)}
               404 {:body (api/response-schema)}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  ;; TODO: implement channel retrieval
  (res/response {:message "not implemented"}))

(defn update-channel
  {:summary "Update a channel by ID"
   :parameters {:path api/IntegrationChannelPathParam
                :body api/IntegrationChannelParams}
   :responses {200 {:Body (api/response-schema)}
               400 {:body (api/response-schema)}}}

  [{:keys [ds bundle-id path-params body] :as _request}]
  ;; TODO: implement channel update
  (res/response {:message "not implemented"}))

(defn delete-channel
  {:summary "Delete a channel by ID"
  :parameters {:path api/IntegrationChannelPathParam}
   :responses {200 {:body (api/response-schema)}
               403 {:body (api/response-schema)}}}

  [{:keys [ds bundle-id path-params user] :as _request}]
  ;; TODO: implement channel deletion
  (res/response {:message "not implemented"}))
