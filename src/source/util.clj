(ns source.util
  (:require [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]))

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

(defn uuid [] 
  (-> 
    (nonce/random-bytes 8)
    (codecs/bytes->hex)))
