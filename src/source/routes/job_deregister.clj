(ns source.routes.job-deregister
  (:require [source.jobs.core :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [js path-params] :as _req}]
  (jobs/deregister! js (:id path-params))
  (res/response {:message "successfully deregistered job"}))
