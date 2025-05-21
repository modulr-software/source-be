(ns source.middleware.auth.util
  (:require [buddy.sign.jwt :as jwt]
            [source.config :as conf]))

(def secret (conf/read-value [:supersecretkey]))
secret
(defn sign-jwt [payload]
  (jwt/encrypt payload secret))

(defn verify-jwt [token]
  (try
    (jwt/decrypt token secret)
    (catch Exception e
      (println e)
      false)))
