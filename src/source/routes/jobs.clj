(ns source.routes.jobs
  (:require [source.services.interface :as services]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [ring.util.response :as res]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [source.util :as util]))

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
                              [:metadata [:map
                                          [:id :string]
                                          [:initial-delay :int]
                                          [:auto-start :boolean]
                                          [:stop-after-fail :boolean]
                                          [:interval :int]
                                          [:recurring? :boolean]
                                          [:args [:map-of :keyword :any]]
                                          [:handler :keyword]
                                          [:sleep :boolean]]]])
   :responses (api/success (api/response-schema))}
  [{:keys [js ds body] :as _req}]
  (let [{:keys [metadata]} body
        metadata (assoc metadata :created-at (util/get-utc-timestamp-string))]
    (congest/register! js (jobs/prepare-congest-metadata ds metadata))
    (res/response {:message "successfully registered job"})))
