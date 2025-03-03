(ns modulr.server
  (:require [modulr.util :as util]
            [org.httpkit.server :as http]
            [ring.middleware.cookies :as cookies]
            [modulr.routes :as routes]))

(defonce ^:private *server (atom nil))

(defn running? []
  (some? @*server))

(defn start-server []
  (cond (not (some? @*server))
        (do
          (println "Starting server on port 3000")
          (reset! *server (http/run-server
                           (->
                            routes/app
                            (util/wrap-json)
                            (cookies/wrap-cookies)) {:port 3000})))
        :else
        (println "Server already running"))
  )

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