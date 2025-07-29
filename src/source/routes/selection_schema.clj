(ns source.routes.selection-schema
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/selection-schema ds)
       (res/response)))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)
        :path-params {:id 1}})
  ())
