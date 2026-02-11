(ns source.jobs.core
  (:require [congest.jobs :as congest]
            [clojure.data.json :as json]
            [source.services.interface :as services]
            [source.jobs.oplog :as oplog]
            [source.jobs.handlers :as handlers]))

(defn prepare-congest-metadata
  "given raw job metadata, returns extended metadata necessary for use with congest"
  [ds store metadata]
  (let [i->b (fn [i] (if (integer? i)
                       (if (= i 1) true false)
                       i))
        args (:args metadata)]
    (-> metadata
        (assoc :auto-start (i->b (:auto-start metadata))
               :stop-after-fail (i->b (:stop-after-fail metadata))
               :recurring? (i->b (or (:recurring metadata) (:recurring? metadata)))
               :sleep (i->b (:sleep metadata))
               :args (if (string? args) (json/read-str (:args metadata) {:key-fn keyword}) args)
               :handler-name (:handler metadata)
               :handler (handlers/handler metadata)
               :logger oplog/operation-logger
               :ds ds
               :store store)
        (dissoc :recurring))))

(defn start!
  "given a job-id, re-registers an existing job from the database"
  [js ds store job-id]
  (let [{:keys [job-metadata-id args handler]} (services/job ds {:where [:= :job-id job-id]})
        metadata (-> (services/job-metadata ds {:id job-metadata-id})
                     (assoc :id job-id
                            :args args
                            :handler handler))]
    (congest/deregister! js job-id)
    (congest/register! js (prepare-congest-metadata ds store metadata))))

(defn interrupted-jobs
  "Get congest-ready metadata of all jobs marked as running"
  [ds store]
  (let [jobs (services/jobs ds)]
    (mapv (fn [{:keys [job-id job-metadata-id args handler status]} i]
            (when (= status "running")
              (let [{:keys [initial-delay
                            interval]
                     :as m} (-> (services/job-metadata ds {:id job-metadata-id})
                                (assoc :id job-id
                                       :args args
                                       :handler handler))
                    metadata (assoc m
                                    :initial-delay (+ initial-delay (* 1000 5 i))
                                    :interval (+ interval (* 1000 5 i)))]
                (prepare-congest-metadata ds store metadata))))
          jobs
          (-> jobs count inc range))))

(comment
  (require '[source.db.util :as db.util]
           '[source.datastore.util :as store.util])

  (def ds (db.util/conn))
  (def store (store.util/conn :datahike))

  (def testjob {:id "test"
                :initial-delay 10
                :auto-start true
                :stop-after-fail false,
                :interval 3000
                :recurring? true
                :args {:name "congest"}
                :handler :test
                :created-at nil
                :sleep false})

  (services/jobs ds)
  (services/job-metadata ds {:id 5})

  (services/delete-job! ds {})
  (services/delete-job-metadata! ds {})

  (def js (congest/create-job-service []))
  (congest/register! js (prepare-congest-metadata ds store testjob))
  (congest/deregister! js "test")
  (congest/stop! js "test" false)
  (start! js ds store "test")
  (interrupted-jobs ds store)

  ())
