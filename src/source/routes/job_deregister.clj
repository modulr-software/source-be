(ns source.routes.job-deregister
  (:require [congest.jobs :as jobs]
            [ring.util.response :as res]
            [source.services.interface :as services]
            [source.routes.openapi :as api]))

(defn get
  {:summary "Deregister a job by id"
   :params (api/params :path [:map [:id :int]])}
  [{:keys [js ds path-params] :as _req}]
  (let [job (services/job ds path-params)]
    (jobs/deregister! js (:job-id job))
    (res/response {:message "successfully deregistered job"})))
