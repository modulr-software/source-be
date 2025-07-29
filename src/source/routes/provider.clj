(ns source.routes.provider
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [store path-params] :as _request}]
  (->> (:id path-params)
       (services/provider store)
       (res/response)))
