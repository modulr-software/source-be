(ns source.routes.providers
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all providers"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:domain [:maybe :string]]
                            [:content-type-id :int]
                            [:instructions [:maybe :string]]
                            [:placeholder-url [:maybe :string]]]]}}}

  [{:keys [ds] :as _request}]
  (-> (hon/find ds {:tname :providers
                    :ret :*})
      (res/response)))

(defn post
  {:summary "add a provider"
   :parameters {:body [:map
                       [:name :string]
                       [:domain {:optional true} [:maybe :string]]
                       [:content-type-id :int]
                       [:instructions {:optional true} [:maybe :string]]
                       [:placeholder-url {:optional true} [:maybe :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]
  (hon/insert! ds {:tname :providers
                   :data body
                   :ret :1})
  (res/response {:message "successfully added provider"}))
