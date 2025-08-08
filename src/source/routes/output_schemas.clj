(ns source.routes.output-schemas
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [store] :as _request}]
  (-> (services/output-schemas store)
      (res/response)))

(defn post [{:keys [store body] :as _request}]
  (let [{:keys [schema]} body]
    (services/add-output-schema! store schema)
    (res/response {:message "successfully added output schema"})))
