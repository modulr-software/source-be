(ns source.middleware.core
  (:require [source.middleware.auth.core :as auth]
            [source.middleware.content-type :as content-type]
            [ring.middleware.json :as ring]
            [ring.middleware.cookies :as cookies]))

(defn apply-generic [app]
  (-> app
      (content-type/wrap-content-type)
      (ring/wrap-json-response)
      (ring/wrap-json-body {:keywords? true})
      (cookies/wrap-cookies)))

(defn apply-auth [app]
  (-> app
      (auth/wrap-auth)))
