(ns source.routes.job-start
  (:require [source.jobs.core :as jobs]
            [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [js ds store path-params]}]
  (let [job (services/job ds path-params)]
    (jobs/start! js ds store (:job-id job))
    (res/response {:message "successfully started job"})))
