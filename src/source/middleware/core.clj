(ns source.middleware.core
  (:require [source.middleware.auth :as auth]
            [source.middleware.content-type :as content-type]
            [source.middleware.json :as json]
            [ring.middleware.cookies :as cookies]))

(defn wrap-middleware [app]
  (-> app
      (content-type/wrap-content-type)
      (json/wrap-json)
      (cookies/wrap-cookies)))


(defn wrap-auth-middlware [app]
  (-> app
      (auth/wrap-auth)))