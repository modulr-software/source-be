(ns source.routes.business
  (:require [source.services.businesses :as businesses]
            [ring.util.response :as res]))

(defn post
  {:summary "insert a business"
   :parameters {:body [:map
                       [:name :string]
                       [:url {:optional true} :string]
                       [:linkedin {:optional true} :string]
                       [:twitter {:optional true} :string]]}
   :responses {201 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]
  (businesses/insert-business! ds body)
  (res/response {:message "successfully added business"}))

(defn patch
  {:summary "update a business by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "business id"} :int]]
                :body [:map
                       [:name :string]
                       [:url {:optional true} :string]
                       [:linkedin {:optional true} :string]
                       [:twitter {:optional true} :string]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds body path-params] :as _request}]
  (businesses/update-business! ds {:id (:id path-params)
                                   :values body})
  (res/response {:message "successfully updated business"}))
