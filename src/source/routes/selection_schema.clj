(ns source.routes.selection-schema
  (:require [ring.util.response :as res]
            [source.workers.xml-schemas :as xml]))

(defn get [{:keys [ds path-params] :as _request}]
  (let [{:keys [output-schema-id] :as selection-schema}
        (xml/selection-schema ds path-params)
        output-schema (xml/output-schema ds output-schema-id)]
    (res/response (merge {:output-schema output-schema}
                         selection-schema))))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)
        :path-params {:id 1}})
  ())
