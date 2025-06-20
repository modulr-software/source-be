(ns source.server
  (:require
   [org.httpkit.server :as http]
   [source.routes.core :as routes]
   [source.util :as util]))

(defonce ^:private *server (atom nil))

(defn running? []
  (some? @*server))

(defn start-server []
  (cond (not (some? @*server))
        (do
          (println "Starting server on port 3000")
          (reset! *server (http/run-server
                           (routes/create-app)
                           {:port 3000})))
        :else
        (println "Server already running")))

(defn stop-server []
  (println "Stopping server...")
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

