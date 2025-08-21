(ns source.jobs.oplog
  (:require [source.services.interface :as services]
            [source.util :as util]
            [clojure.data.json :as json]))

(defn delete-job! [{:keys [ds id]}]
  (let [metadata-id (:job-metadata-id (services/job ds {:id id}))]
    (services/delete-job-metadata! ds {:id metadata-id})
    (services/delete-job! ds {:id id})))

(defn update-job! [{:keys [ds id]} status]
  (services/update-job! ds {:id id
                            :data {:status status
                                   :last-heartbeat (util/get-utc-timestamp-string)}}))

(defn update-job-metadata! [{:keys [ds id num-calls]}]
  (let [metadata-id (:job-metadata-id (services/job ds {:id id}))]
    (services/update-job-metadata! ds {:id metadata-id
                                       :data {:num-calls num-calls}})))

(defn create-job! [{:keys [ds id args handler-name] :as job-metadata}]
  (let [b->i (fn [b] (if b 1 0))
        metadata {:initial-delay (:initial-delay job-metadata)
                  :auto-start (b->i (:auto-start job-metadata))
                  :stop-after-fail (b->i (:stop-after-fail job-metadata))
                  :kill-after (:kill-after job-metadata)
                  :num-calls (:num-calls job-metadata)
                  :interval (:interval job-metadata)
                  :recurring (b->i (:recurring? job-metadata))
                  :created-at (:created-at job-metadata)
                  :sleep (b->i (:sleep job-metadata))}
        job (services/job ds {:id id})
        metadata-id (when-not (some? job)
                      (:id (services/insert-job-metadata! ds {:data metadata
                                                              :ret :1})))]
    (if (some? job)
      (services/update-job! ds {:id id
                                :data {:status "running"
                                       :last-heartbeat (util/get-utc-timestamp-string)}})
      (services/insert-job! ds {:data {:id id
                                       :status "running"
                                       :args (json/json-str args)
                                       :handler (name handler-name)
                                       :last-heartbeat (util/get-utc-timestamp-string)
                                       :job-metadata-id metadata-id}
                                :ret :1}))))

(defn operation-logger [{:keys [action] :as metadata}]
  (cond
    (= action "register")
    (create-job! metadata)

    (= action "deregister")
    (delete-job! metadata)

    (= action "start")
    (update-job! metadata "running")

    (or (= action "stop") (= action "kill"))
    (update-job! metadata "stopped")

    (= action "run")
    (do
      (update-job! metadata "running")
      (update-job-metadata! metadata))))
