(ns source.routes.selection-schema
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [ds path-params store] :as _request}]
  (let [{:keys [output-schema-id] :as selection-schema}
        (services/selection-schema ds (:id path-params))
        output-schema (services/output-schema store output-schema-id)]
    (res/response (merge {:output-schema output-schema}
                         selection-schema))))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)
        :path-params {:id 1}})
  ())
