(ns modulr.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [modulr.users :as users]))

(def home (GET "/" []
            {:status 200
                       :body {:value "bye bye"}
                       :headers {"Content-Type" "application/json"}
                       :cookies {"test-session" {:value "test"}}}
            ))

(def login
  (POST "/login" request
    (println "in login")
    (users/create-user (:body request))
    {:status 200}))

(def users
  (GET "/users" []
    {:status 200
     :body {:users (users/get-users)}}))


(defroutes app
  home
  login
  users
  (route/not-found "Page not found"))

(comment
(def instance '(defroutes app
                  home
                  login
                  users
                  (route/not-found "Page not found")))
  )
