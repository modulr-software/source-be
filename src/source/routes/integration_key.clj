(ns source.routes.integration-key
  (:require [source.middleware.auth.util :as auth.util]
            [ring.util.response :as res]))

(defn post
  {:summary "generate an API key for the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map [:key :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [user path-params] :as _request}]
  (let [api-key (auth.util/sign-jwt {:user-id (:id user)
                                     :bundle-id (:id path-params)})]
    (res/response {:key api-key})))
