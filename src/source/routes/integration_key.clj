(ns source.routes.integration-key
  (:require [ring.util.response :as res]
            [source.workers.integrations :as integrations]))

(defn post
  {:summary "generate an API key for the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:map [:key :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user path-params] :as _request}]
  (->> (integrations/generate-api-key! ds (:id user) (:id path-params))
       (assoc {} :key)
       (res/response)))
