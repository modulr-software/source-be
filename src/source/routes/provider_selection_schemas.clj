(ns source.routes.provider-selection-schemas
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (->> (:id path-params)
       (assoc {} :provider-id)
       (services/selection-schemas-by-provider ds)
       (res/response)))
