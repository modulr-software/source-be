(ns source.routes.rss
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post-output-schema [{:keys [ds body] :as _request}]
  (let [{:keys [key value]} body]
    (services/insert-output-schema! ds {:key key
                                        :value value})))

(defn selection-schemas [{:keys [store] :as _request}]
  (services/selection-schemas store))

(defn output-schemas [{:keys [store] :as _request}]
  (services/output-schemas store))

(defn output-schemas [])

(defn get-ast [])

(defn get-data [])
