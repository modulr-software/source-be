(ns source.routes.content-types
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (-> (services/content-types ds)
      (res/response)))
