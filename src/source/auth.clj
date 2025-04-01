(ns source.auth
  (:require [source.password :as pw]
            [buddy.sign.jwt :as jwt]
            [source.config :as conf]))

(def secret (:supersecretkey conf/config))

(defn sign-jwt [payload]
  (jwt/encrypt payload secret))

(defn verify-jwt [token]
  (try
    (jwt/decrypt token secret)
    (catch Exception e
      (println e)
      false)))


(defn create-session [user]
  (let [payload {:id (:id user)
                 :role (:role user)}]
    {:access-token (sign-jwt payload)
     :refresh-token (sign-jwt payload)})
  )

(defn validate-session [token]
  (let [decoded (verify-jwt token)]
    (if (nil? decoded)
      {:code 401 :status "error" :message "Invalid token!"}
      decoded)))