(ns modulr.util
  (:require [jsonista.core :as json]))

(defn- get-content-type [request]
  (or (get-in request [:headers "Content-Type"])
      (get-in request [:headers :content-type])
      (get-in request [:headers "content-type"])
      (get-in request [:headers :Content-Type])))

(defn- decode-body-json [request]
  (let [content-type (get-content-type request)
        body (get-in request [:body])]
    (println (get-in request [:headers]))
    (cond (and (= content-type "application/json") (some? body))
          (assoc request :body (json/read-value body json/keyword-keys-object-mapper))
          :else
          request)))

(defn- encode-body-json [request]
  (let [content-type (get-content-type request)
        body (get-in request [:body])]
    (cond (and (= content-type "application/json") (some? body))
          (assoc request :body (json/write-value-as-string body json/keyword-keys-object-mapper))
          :else
          request)))

(defn wrap-json [handler]
  (fn [request]
  (let [body (slurp (:body request))]
    (println (json/read-value body json/keyword-keys-object-mapper))
    (encode-body-json
     (handler (decode-body-json
               (assoc
                request
                :body
                body)))))))

(defn string->stream
  ([s] (string->stream s "UTF-8"))
  ([s encoding]
   (-> s
       (.getBytes encoding)
       (java.io.ByteArrayInputStream.))))

(comment
  ((wrap-json (fn [request] request)) {:headers {"Content-Type" "application/json"}
                                       :body (string->stream "{\"username\": \"merve\"}")}))

