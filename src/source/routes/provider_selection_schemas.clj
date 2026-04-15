(ns source.routes.provider-selection-schemas
  (:require [ring.util.response :as res]
            [source.workers.xml-schemas :as xml]))

(defn get [{:keys [ds path-params] :as _request}]
  (let [selection-schemas (xml/selection-schemas
                           ds
                           {:where [:= :provider-id (Integer/parseInt (:id path-params))]})
        results (mapv (fn [{:keys [output-schema-id] :as ss}]
                        (merge
                         ss
                         {:output-schema
                          (xml/output-schema ds output-schema-id)}))
                      selection-schemas)]
    (res/response results)))
