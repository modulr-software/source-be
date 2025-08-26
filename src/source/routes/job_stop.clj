(ns source.routes.job-stop
  (:require [congest.jobs :as jobs]
            [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [js ds path-params]}]
  (let [job (services/job ds path-params)]
    (jobs/stop! js (:job-id job) false)
    (res/response {:message "successfully stopped job"})))
