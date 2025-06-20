(ns source.routes.core
  (:require [compojure.core :refer [routes GET POST PATCH]]
            [compojure.route :as route]
            [source.middleware.auth.util :as auth.util]
            [source.middleware.interface :as mw]
            [source.routes.users :as users]
            [source.routes.google :as google]
            [source.routes.protected :as protected]
            [source.routes.admin :as admin]))

(defn home [_req]
  {:status 200
   :body {:value "bye bitch"}
   :cookies {"test-session" {:value "test"}}})

(defn create-app []
  (mw/apply-generic
   (routes
    (GET "/" req [] (home req))

    (POST "/login" req [] (users/login req))
    (POST "/register" req [] (users/register req))
    (GET "/users" req [] (users/users req))
    (PATCH "/users/:id" req [] (users/update-user req))

    (GET "/oauth2/google" req [] (google/google-launch req))
    (GET "/oauth2/google/callback" req [] (google/google-redirect req))

    (->
     (routes
      (GET "/authorized" req [] (protected/authorized req)))
     (mw/apply-auth))

    (->
     (routes
      (POST "/add-admin" req [] (admin/add-admin req)))
     (mw/apply-auth {:required-type :admin}))

    (route/not-found "Page not found"))))

(comment
  (let [app (create-app)
        req {:uri "/authorized"
             :request-method :get
             :headers {"authorization"
                       (str "Bearer " (auth.util/sign-jwt {:id 1}))}}]
    (app req)))

