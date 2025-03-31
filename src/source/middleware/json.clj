(ns source.middleware.json
  (:require [source.util :as util]
            [jsonista.core :as json]))


(defn- decode-body-json [request]
  (let [content-type (util/request->content-type request)
        body (get-in request [:body])]
    (cond (and (= content-type "application/json") (some? body))
          (assoc request :body (json/read-value body json/keyword-keys-object-mapper))
          :else
          request)))

(defn- encode-body-json [request]
  (let [content-type (util/request->content-type request)
        body (get-in request [:body])]
    (cond (and (= content-type "application/json") (some? body))
          (assoc request :body (json/write-value-as-string body json/keyword-keys-object-mapper))
          :else
          request)))

(defn wrap-json [handler]
  (fn [request]
  (let [body (when (some? (:body request)) (slurp (:body request)))]
    (encode-body-json
     (handler (decode-body-json
               (assoc
                request
                :body
                body)))))))