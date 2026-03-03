(ns source.routes.job-stop
  (:require [congest.jobs :as jobs]
            [ring.util.response :as res]
            [source.services.interface :as services]
            [source.routes.openapi :as api]))

(defn get
  {:summary "stop a job by id"
   :params (api/params :path [:map [:id :int]])}
  [{:keys [js ds path-params]}]
  (let [job (services/job ds path-params)]
    (jobs/stop! js (:job-id job) false)
    (res/response {:message "successfully stopped job"})))
