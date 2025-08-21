(ns source.routes.provider
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get provider by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "provider id"} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]
                           [:domain :string]
                           [:content-type-id :int]]}}}

  [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/provider ds)
       (res/response)))

(defn delete 
  {:summary "given a provider id, delete provider and associated selection schemas"
   :parameters {:path [:map [:id {:title "id"
                                  :description "provider id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}}}
  
  [{:keys [store ds path-params] :as _request}]
  (->> path-params
       (services/delete-provider! ds))
  (services/delete-selection-schemas-by-provider! store ds (:id path-params))
  (res/response {:message "successfully deleted provider"}))
