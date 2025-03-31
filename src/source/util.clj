(ns source.util
  (:require [jsonista.core :as json]
            [clojure.string :as str]))

(defn request->auth-header [request]
  (or (get-in request [:headers "Authorization"])
      (get-in request [:headers :Authorization])
      (get-in request [:headers :authorization])))

(defn request->auth-token [request]
    (when-let [auth-token (request->auth-header request)
               ]
      (cond
        (some? auth-token)
        (when-let [token
                   (-> auth-token (str/split #"Bearer\s") last)]
          token)
        :else nil)))

(defn request->content-type [request]
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