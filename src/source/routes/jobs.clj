(ns source.routes.jobs
  (:require [source.services.interface :as services]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [ring.util.response :as res]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [source.util :as util]
            [malli.util :as mu]))

(defn get
  {:summary "get list of raw job metadata"
   :responses (api/success schemas/JobsWithMetadata)}
  [{:keys [ds] :as _req}]
  (->>
   (services/jobs ds)
   (mapv (fn [job]
           (let [metadata (services/job-metadata ds {:id (:job-metadata-id job)})]
             (merge (dissoc job :job-metadata-id)
                    {:metadata metadata}))))
   (res/response)))

(defn post
  {:summary "Register a new job with metadata"
   :params (api/params :body [:map
                              [:metadata
                               (-> schemas/JobMetadata
                                   (api/missoc :recurring :num-calls)
                                   (mu/assoc :auto-start :boolean)
                                   (mu/assoc :stop-after-fail :boolean)
                                   (mu/assoc :recurring? :boolean)
                                   (mu/assoc :sleep :boolean)
                                   (mu/assoc :handler :string))]])
   :responses (api/success (api/response-schema))}
  [{:keys [js ds body] :as _req}]
  (let [{:keys [metadata]} body
        metadata (assoc metadata :created-at (util/get-utc-timestamp-string))]
    (congest/register! js (jobs/prepare-congest-metadata ds js metadata))
    (res/response {:message "successfully registered job"})))
