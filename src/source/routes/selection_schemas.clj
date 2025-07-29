(ns source.routes.selection-schemas
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds] :as _request}]
  (-> (services/selection-schemas ds)
      (res/response)))

(defn post [{:keys [store ds body] :as _request}]
  (let [{:keys [_schema _record] :as opts} body]
    (-> (services/add-selection-schema! store ds opts)
        (res/response))))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
