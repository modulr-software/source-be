(ns source.routes.integrations
  (:require [ring.util.response :as res]
            [source.services.bundles :as bundles]
            [source.workers.integrations :as integrations]
            [source.util :as util]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [congest.jobs :as congest]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [source.db.honey :as hon]
            [malli.util :as mu]))

(defn get
  {:summary "Get metadata of all integrations on the user account"
   :responses {200 {:body schemas/Bundles}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user] :as _request}]
  (res/response (bundles/bundles ds {:where [:= :user-id (:id user)]})))

(defn post
  {:summary "Creates an integration and the associated bundle in which content is stored"
   :description "When an integration is created, a job is scheduled to periodically run post selection every 24 hours. During post selection, the bundle is filled with relevant content according to desired categories, content types and analytics."
   :parameters {:body (-> [:map
                           [:name :string]
                           [:ts-and-cs {:optional true} :int]
                           [:integration-type-id :int]]
                          (mu/assoc :content-types [:vector schemas/ConstantSchema])
                          (mu/assoc :categories [:vector schemas/ConstantSchema]))}
   :responses {201 {:body schemas/Bundle}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [js ds user body] :as _request}]
  (let [new-bundle (->> {:user-id (:id user)
                         :bundle-metadata (dissoc body :categories :content-types)
                         :categories (:categories body)
                         :content-types (:content-types body)}
                        (integrations/create-integration! ds))
        categories-by-bundle (bundles/categories-in-bundle ds (:id new-bundle))]
    ;TODO: service needed
    (->> (jobs/prepare-congest-metadata
          ds
          js
          {:id (handlers/update-bundle-job-id (:id new-bundle))
           :initial-delay 0
           :auto-start true
           :stop-after-fail false,
           :interval (* 1000 60 60 24)
           :recurring? true
           :args {:bundle-id (:id new-bundle)
                  :categories categories-by-bundle}
           :handler :update-bundle
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))
    (res/response new-bundle)))

(defn get-integration-types
  {:summary "get all integration types"
   :responses (api/success schemas/IntegrationTypes)}
  [{:keys [ds]}]
  (res/response (hon/find ds {:tname :integration-types})))
