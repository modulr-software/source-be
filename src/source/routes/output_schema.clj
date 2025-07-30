(ns source.routes.output-schema
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [store path-params] :as _request}]
  (->> (:id path-params)
       (Integer/parseInt)
       (services/output-schema store)
       (res/response)))
