(ns source.routes.output-schemas
  (:require [source.workers.xml-schemas :as xml]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (-> (xml/output-schemas ds)
      (res/response)))

(defn post [{:keys [ds body] :as _request}]
  (let [{:keys [schema]} body]
    (xml/insert-output-schema! ds schema)
    (res/response {:message "successfully added output schema"})))
