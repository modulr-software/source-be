(ns source.util
  (:require [jsonista.core :as json]
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

(defn content-type [request]
  (or (get-in request [:headers "Content-Type"])
      (get-in request [:headers :content-type])
      (get-in request [:headers "content-type"])
      (get-in request [:headers :Content-Type])))

(defn unwrap-keys [m]
  (if (some? m)
    (update-keys m (fn [k] (keyword (name k)))) nil))

(defn prr [value]
  (println value)
  value)
