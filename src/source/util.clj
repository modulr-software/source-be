(ns source.util
  (:require [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [clojure.main :refer [demunge]]
            [malli.core :as m]
            [malli.error :as me])
  (:import (java.math BigInteger)
           (java.security MessageDigest)))

(defn vectors?
  "Returns true if coll is a 2d vector"
  [coll]
  (and (vector? coll)
       (vector? (first coll))))

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

(defn get-utc-timestamp-string []
  (let [formatter (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss'Z'")
        utc-now (java.time.ZonedDateTime/now java.time.ZoneOffset/UTC)]
    (.format formatter utc-now)))

(defn uuid []
  (->
   (nonce/random-bytes 8)
   (codecs/bytes->hex)))

(defn metadata [func]
  (-> (class func)
      (print-str)
      (demunge)
      (symbol)
      (find-var)
      (meta)))

(defn sha256
  "Computes SHA256 hash of given string and returns it as a hex string"
  [s]
  (let [digest (MessageDigest/getInstance "SHA-256")
        bytes (.getBytes s "UTF-8")
        hash-bytes (.digest digest bytes)]
    (format "%064x" (BigInteger. 1 hash-bytes))))

(defn validate [handler data]
  (let [schema (get-in (metadata handler) [:parameters :body])
        success (m/validate schema data)]
    {:data (when success data)
     :success success
     :error (when-not success (->> data
                                   (m/explain schema)
                                   (me/humanize)))}))

(defn format-rss-date
  "Takes a date as a string in RFC 1123 format and returns it in a format that meets ISO 8601 standards for SQLite.
  This is necessary because some RSS feeds use a different date than what is accepted by SQLite.
  Returns the original string if it is not in this format."
  [s]
  (try
    (let [zdt (java.time.ZonedDateTime/parse s java.time.format.DateTimeFormatter/RFC_1123_DATE_TIME)
          zdt-utc (.withZoneSameInstant zdt (java.time.ZoneId/of "UTC"))
          out (.format zdt-utc (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))]
      out)
    (catch Exception _ s)))

(comment
  (require '[source.routes.business :as business])
  (validate business/post {:cheese "modulr"})
  (sha256 "1")
  ())

