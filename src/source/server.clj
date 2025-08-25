(ns source.server
  (:require [org.httpkit.server :as http]
            [source.db.interface :as db]
            [source.datastore.interface :as store]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [source.routes.interface :as routes]
            [source.util :as util]))

(defonce ^:private *server (atom nil))

(defonce ^:private *components (atom nil))

(defn initialise-components! []
  (if (some? @*components)
    (do
      (println "Components already initialised.")
      (let [{:keys [ds store]} @*components]
        (->> (jobs/interrupted-jobs ds store)
             (congest/create-job-service)
             (swap! *components assoc :js))))

    (try
      (println "Initialising components...")
      (let [{:keys [ds store]} (reset! *components {:ds (db/ds :master)
                                                    :store (store/ds :datahike)})]
        (->> (jobs/interrupted-jobs ds store)
             (congest/create-job-service)
             (swap! *components assoc :js)))
      (catch Exception e (println "Failed to initialise components: " (.getMessage e))))))

(defn running? []
  (some? @*server))

(defn start-server []
  (cond (not (some? @*server))
        (do
          (println "Starting server on port 3000...")
          (initialise-components!)
          (reset! *server (http/run-server
                           (routes/create-app @*components)
                           {:port 3000})))
        :else
        (println "Server already running!")))

(defn stop-server []
  (println "Stopping server...")
  (when (some? @*components)
    (congest/kill! (:js @*components)))
  (when (some? @*server)
    (@*server))
  (reset! *server nil))

(defn restart-server []
  (stop-server)
  (start-server))

(comment
  (def test-wrapper
    (util/wrap-json (fn [request] request)))
  (test-wrapper {:status 200
                 :body "{\"value\":\"Hello, Source!\"}"
                 :headers {"Content-Type" "application/json"}}))
