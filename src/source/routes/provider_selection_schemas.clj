(ns source.routes.provider-selection-schemas
  (:require [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get [{:keys [ds path-params store] :as _request}]
  (let [selection-schemas (->>
                           (:id path-params)
                           (assoc {} :provider-id)
                           (services/selection-schemas-by-provider ds))
        results (mapv (fn [{:keys [output-schema-id] :as ss}]
                        (merge
                         ss
                         {:output-schema
                          (services/output-schema store output-schema-id)}))
                      selection-schemas)]
    (res/response results)))
