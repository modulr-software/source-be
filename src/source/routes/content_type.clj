(ns source.routes.content-type
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/content-type ds)
       (res/response)))
