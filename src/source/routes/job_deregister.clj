(ns source.routes.job-deregister
  (:require [congest.jobs :as jobs]
            [ring.util.response :as res]))

(defn get [{:keys [js path-params] :as _req}]
  (jobs/deregister! js (:id path-params))
  (res/response {:message "successfully deregistered job"}))
