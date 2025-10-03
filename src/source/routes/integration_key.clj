(ns source.routes.integration-key
  (:require [source.util :as util]
            [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post
  {:summary "generates an API key for an integration by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map [:hash :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (let [api-key (util/uuid)] ; TODO: make this actually generate a hash for the API key
    (services/update-bundle! ds {:id (:id path-params)
                                 :data {:hash api-key}})
    (res/response {:hash api-key})))
