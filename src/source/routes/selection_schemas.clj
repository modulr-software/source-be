(ns source.routes.selection-schemas
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [ds] :as _request}]
  (-> (services/selection-schemas ds)
      (res/response)))

(defn post [{:keys [store ds body] :as _request}]
  (-> (services/add-selection-schema! store ds body)
      (res/response)))


(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
