(ns source.routes.jobs
  (:require [source.services.interface :as services]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _req}]
  (->>
   (services/jobs ds)
   (mapv (fn [job]
           (let [metadata (services/job-metadata ds {:id (:job-metadata-id job)})]
             (merge (dissoc job :job-metadata-id)
                    {:metadata metadata}))))
   (res/response)))

(defn post [{:keys [js ds body] :as _req}]
  (let [{:keys [metadata]} body]
    (congest/register! js (jobs/prepare-congest-metadata ds metadata))
    (res/response {:message "successfully registered job"})))
