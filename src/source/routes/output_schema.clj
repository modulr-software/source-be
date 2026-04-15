(ns source.routes.output-schema
  (:require [source.workers.xml-schemas :as xml]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (let [id (try
             (Integer/parseInt (:id path-params))
             (catch Exception _ nil))]
    (if (some? id)
      (->> id
           (xml/output-schema ds)
           (res/response))
      (-> (res/response {:message "invalid id"})
          (res/status 400)))))
