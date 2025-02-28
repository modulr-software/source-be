(ns modulr.server
  (:require [modulr.util :as util]
            [org.httpkit.server :as http]
            [ring.middleware.cookies :as cookies]
            [modulr.routes :as routes]))

(def ^:private *server (atom nil))

(defn running? []
  (some? @*server))

(defn start-server []
  (println "Starting server on port 3000")
  (reset! *server (http/run-server
                   (->
                    routes/app
                    (util/wrap-json)
                    (cookies/wrap-cookies)
                    ) {:port 3000})))

(defn stop-server []
  (println "Stopping server...")
  (when (some? @*server)
    (@*server))
  (reset! *server nil))

(comment
  (def test-wrapper
    (util/wrap-json (fn [request] request)))
  (test-wrapper {:status 200
                 :body "{\"value\":\"Hello, Source!\"}"
                 :headers {"Content-Type" "application/json"}}))