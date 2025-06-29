(ns source.routes.selection-schema
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post [{:keys [store ds body] :as _request}]
  (let [{:keys [schema record] :as opts} body]
    (-> (services/add-selection-schema! store ds opts)
        (res/response))))
