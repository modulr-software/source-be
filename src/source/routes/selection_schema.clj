(ns source.routes.selection-schema
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post [{:keys [store ds body] :as _request}]
  (let [{:keys [_schema _record] :as opts} body]
    (-> (services/add-selection-schema! store ds opts)
        (res/response))))

(defn get [{:keys [ds path-params] :as _request}]
  (->> path-params
       (services/selection-schema ds)
       (res/response)))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)
        :path-params {:id 1}})
  ())
