(ns source.middleware.core
  (:require [source.middleware.auth.core :as auth]
            [source.middleware.content-type :as content-type]
            [source.middleware.json :as json]
            [ring.middleware.cookies :as cookies]))

(defn apply-generic [app]
  (-> app
      (content-type/wrap-content-type)
      (json/wrap-json)
      (cookies/wrap-cookies)))

(defn apply-auth [app]
  (-> app
      (auth/wrap-auth)))
