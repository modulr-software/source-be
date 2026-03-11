(ns source.jobs.core
  (:require [congest.jobs :as congest]
            [clojure.data.json :as json]
            [source.services.interface :as services]
            [source.jobs.oplog :as oplog]
            [source.jobs.handlers :as handlers]
            [source.db.honey :as hon]))

(defn prepare-congest-metadata
  "given raw job metadata, returns extended metadata necessary for use with congest"
  [ds js metadata]
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
               :js js)
        (dissoc :recurring))))

(defn start!
  "given a job-id, re-registers an existing job from the database"
  [js ds job-id]
  (let [{:keys [job-metadata-id args handler]} (services/job ds {:where [:= :job-id job-id]})
        metadata (-> (services/job-metadata ds {:id job-metadata-id})
                     (assoc :id job-id
                            :args args
                            :handler handler))]
    (congest/deregister! js job-id)
    (congest/register! js (prepare-congest-metadata ds js metadata))))

(defn interrupted-jobs
  "Get vec of congest-ready metadata of all jobs marked as running"
  [ds js]
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
                                    :initial-delay (if (some? initial-delay)
                                                     (+' initial-delay (*' 1000 5 i))
                                                     0)
                                    :interval (if (some? interval)
                                                (+' interval (*' 1000 5 i))
                                                0))]
                (prepare-congest-metadata ds js metadata))))
          jobs
          (-> jobs count inc range))))

(comment
  (require '[source.db.util :as db.util])

  (def ds (db.util/conn))

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
  (congest/register! js (prepare-congest-metadata ds js testjob))
  (congest/deregister! js "delete_creator_31")
  (congest/stop! js "test" false)
  (start! js ds "test")
  (interrupted-jobs ds js)

  ())
