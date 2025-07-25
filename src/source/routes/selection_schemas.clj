(ns source.routes.selection-schemas
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (-> (services/selection-schemas ds)
      (res/response)))
