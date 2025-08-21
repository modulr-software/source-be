(ns source.jobs.core
  (:require [congest.jobs :as congest]
            [clojure.data.json :as json]
            [source.services.interface :as services]
            [source.jobs.oplog :as oplog]
            [source.jobs.handlers :as handlers]))

(defn start! [js ds store job-id]
  (let [i->b (fn [i] (if (= i 1) true false))
        job (services/job ds {:id job-id})
        job-metadata (services/job-metadata ds {:id (:job-metadata-id job)})
        metadata (-> (assoc job-metadata
                            :id (str (:id job))
                            :auto-start (i->b (:auto-start job-metadata))
                            :stop-after-fail (i->b (:stop-after-fail job-metadata))
                            :recurring? (i->b (:recurring job-metadata))
                            :sleep (i->b (:sleep job-metadata))
                            :args (json/read-str (:args job) {:key-fn keyword})
                            :handler-name (:handler job)
                            :handler (handlers/handler job)
                            :logger oplog/operation-logger
                            :ds ds
                            :store store)
                     (dissoc :recurring))]
    (congest/deregister! js (str (:id job)))
    (congest/register! js metadata)))

(defn register!
  "Registers a new job with the given job metadata, handler and arguments"
  [js ds store job-metadata args]
  (let [handler-fn (handlers/handler job-metadata)
        job-id (-> (services/jobs ds)
                   (count)
                   (inc))
        job-metadata (assoc job-metadata
                            :id (str job-id)
                            :args args
                            :ds ds
                            :store store
                            :handler-name (:handler job-metadata)
                            :handler handler-fn
                            :logger oplog/operation-logger)]
    (congest/register! js job-metadata)))

(defn deregister! [js job-id]
  (congest/deregister! js (str job-id)))

(defn stop!
  ([js job-id]
   (stop! js job-id false))
  ([js job-id kill?]
   (congest/stop! js (str job-id) kill?)))

(defn kill! [js job-id]
  (stop! js job-id true))

(defn kill-all! [js]
  (congest/kill! js))

(defn start-interrupted-jobs!
  "Starts all jobs with a status of 'running'. Intended for server startup to restart interrupted jobs."
  [js ds store]
  (let [jobs (services/jobs ds)]
    (run! (fn [job]
            (when (= (:status job) "running")
              (start! js ds store (:id job)))) jobs)))

(comment
  (require '[source.db.util :as db.util]
           '[source.datastore.util :as store.util])
  (def ds (db.util/conn))
  (def store (store.util/conn :datahike))

  (def testjob {:initial-delay 10
                :auto-start true
                :stop-after-fail false,
                ;:kill-after 5
                ;:num-calls nil
                :interval 3000
                :recurring? true
                :handler :test
                :created-at nil
                :sleep false})

  (services/jobs ds)
  (services/job-metadata ds {:id 3})

  (services/delete-job! ds {})
  (services/delete-job-metadata! ds {})

  (def js (congest/create-job-service []))
  (register! js ds store testjob {:name "congest"})
  (deregister! js 1)
  (stop! js 1)
  (start! js ds store 1)
  (start-interrupted-jobs! js ds store)

  ())
