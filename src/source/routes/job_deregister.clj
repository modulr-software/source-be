(ns source.routes.job-deregister
  (:require [congest.jobs :as jobs]
            [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [js ds path-params] :as _req}]
  (let [job (services/job ds path-params)]
    (jobs/deregister! js (:job-id job))
    (res/response {:message "successfully deregistered job"})))
