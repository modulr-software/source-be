(ns source.routes.providers
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all providers"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:domain :string]
                            [:content-type-id :int]]]}}}

  [{:keys [ds] :as _request}]
  (-> (services/providers ds)
      (res/response)))

(defn post
  {:summary "add a provider"
   :parameters {:body [:map
                       [:provider
                        [:map
                         [:id :int]
                         [:name :string]
                         [:domain :string]
                         [:content-type-id :int]]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]
  (let [{:keys [provider]} body]
    (services/insert-provider! ds {:data provider
                                   :ret :1})
    (res/response {:message "successfully added provider"})))
