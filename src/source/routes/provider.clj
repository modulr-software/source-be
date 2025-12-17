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
                           [:domain [:maybe :string]]
                           [:content-type-id :int]
                           [:instructions [:maybe :string]]
                           [:placeholder-url [:maybe :string]]]}}}

  [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/provider ds)
       (res/response)))

(defn post
  {:summary "update provider by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "provider id"} :int]]
                :body [:map
                       [:name :string]
                       [:domain {:optional true} [:maybe :string]]
                       [:content-type-id :int]
                       [:instructions {:optional true} [:maybe :string]]
                       [:placeholder-url {:optional true} [:maybe :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (services/update-provider! ds {:id (:id path-params)
                                 :data body})
  (res/response {:message "successfully updated provider"}))

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
