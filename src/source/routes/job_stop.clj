(ns source.routes.job-stop
  (:require [source.jobs.core :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [js path-params]}]
  (jobs/stop! js (:id path-params))
  (res/response {:message "successfully stopped job"}))
