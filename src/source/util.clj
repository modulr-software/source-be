(ns source.util
  (:require [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [clojure.main :refer [demunge]]
            [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]))

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

(defn validate [handler data]
  (let [schema (get-in (metadata handler) [:parameters :body])
        decoded (m/decode schema data mt/string-transformer)
        success (m/validate schema decoded)]
    {:data decoded
     :success success
     :error (when-not success (->> decoded
                                   (m/explain schema)
                                   (me/humanize)))}))

(comment 
  (require '[source.routes.business :as business])
  (validate business/post {:name "modulr"})
  ())

