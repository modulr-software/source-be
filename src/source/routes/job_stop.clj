(ns source.routes.job-stop
  (:require [congest.jobs :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [js path-params]}]
  (jobs/stop! js (:id path-params) false)
  (res/response {:message "successfully stopped job"}))
