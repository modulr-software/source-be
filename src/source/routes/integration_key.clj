(ns source.routes.integration-key
  (:require [source.util :as util]
            [ring.util.response :as res]
            [source.services.interface :as services]))

(defn post
  {:summary "generate an API key for the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map [:key :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user path-params] :as _request}]
  (let [uuid (util/uuid)
        api-key (util/sha256 (str (:id user) (:id path-params) uuid))]
    (services/update-bundle! ds {:id (:id path-params)
                                 :data {:hash api-key}})
    (res/response {:key api-key})))
