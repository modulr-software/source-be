(ns source.routes.data
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post [{:keys [store body] :as _request}]
  (let [{:keys [_schema-id _url] :as opts} body]
    (-> (services/extract-data store opts)
        (res/response))))
