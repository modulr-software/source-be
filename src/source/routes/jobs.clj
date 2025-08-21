(ns source.routes.jobs
  (:require [source.services.interface :as services]
            [source.jobs.core :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _req}]
  (res/response
   (mapv (fn [job]
           (let [metadata (services/job-metadata ds {:id (:job-metadata-id job)})]
             (merge (dissoc job :job-metadata-id)
                    {:metadata metadata}))) (services/jobs ds))))

(defn post [{:keys [js ds store body] :as _req}]
  (let [{:keys [metadata args]} body]
    (jobs/register! js ds store metadata args)
    (res/response {:message "successfully registered job"})))
