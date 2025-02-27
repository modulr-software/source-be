(ns modulr.source
  (:require [org.httpkit.server :as http]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [modulr.db :as db]
            [ring.middleware.cookies :as cookies]
            [jsonista.core :as json]))

(db/create-db)

(def *server (atom nil))

(def home (GET "/" [] {:status 200
                       :body (json/write-value-as-string {:value "Hello, Source!"} json/keyword-keys-object-mapper)
                       :headers {"Content-Type" "application/json"}}))

(defroutes app
  home
  (route/not-found "Page not found"))

(defn start-server []
  (println "Starting server on port 8080")
  (reset! *server (http/run-server
                   (-> app
                       (cookies/wrap-cookies)
                       )
                   {:port 3000})))

(defn stop-server []
  (when (some? @*server)
    (@*server))
  (reset! *server nil))

(start-server)
(stop-server)
(println *server)

(defn -main [& _]
  (println "Hello, Source!"))

(-main)