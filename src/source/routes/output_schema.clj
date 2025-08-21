(ns source.routes.output-schema
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [store path-params] :as _request}]
  (let [id (try
             (Integer/parseInt (:id path-params))
             (catch Exception _ nil))]
    (if (some? id)
      (->> id
           (services/output-schema store)
           (res/response))
      (-> (res/response {:message "invalid id"})
          (res/status 400)))))
