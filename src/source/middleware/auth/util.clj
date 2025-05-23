(ns source.middleware.auth.util
  (:require [buddy.sign.jwt :as jwt]
            [source.config :as conf]
            [clojure.string :as str]))

(defn auth-header [request]
  (or (get-in request [:headers "Authorization"])
      (get-in request [:headers :Authorization])
      (get-in request [:headers :authorization])))

(defn auth-token [request]
  (when-let [auth-token (auth-header request)]
    (cond
      (some? auth-token)
      (when-let [token
                 (-> auth-token (str/split #"Bearer\s") last)]
        token)
      :else nil)))

(def secret (conf/read-value [:supersecretkey]))

(defn sign-jwt [payload]
  (jwt/encrypt payload secret))

(defn verify-jwt [token]
  (try
    (jwt/decrypt token secret)
    (catch Exception e
      (println (.getMessage e))
      false)))
