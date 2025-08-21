(ns source.routes.job-start
  (:require [source.jobs.core :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [js ds store path-params]}]
  (jobs/start! js ds store (:id path-params))
  (res/response {:message "successfully started job"}))
