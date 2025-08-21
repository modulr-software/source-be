(ns source.routes.selection-schema
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [ds path-params store] :as _request}]
  (let [{:keys [output-schema-id] :as selection-schema}
        (services/selection-schema ds path-params)
        output-schema (services/output-schema store output-schema-id)]
    (res/response (merge {:output-schema output-schema}
                         selection-schema))))

(comment
  (require '[source.db.util :as db.util]
           '[source.datastore.util :as store.util])
  (get {:ds (db.util/conn)
        :store (store.util/conn :datahike)
        :path-params {:id 1}})
  ())
