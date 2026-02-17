(ns source.routes.selection-schemas
  (:require [ring.util.response :as res]
            [source.workers.xml-schemas :as xml]))

(defn get [{:keys [ds] :as _request}]
  (-> (xml/selection-schemas ds)
      (res/response)))

(defn post [{:keys [ds body] :as _request}]
  (-> (xml/insert-selection-schema! ds body)
      (res/response)))

(comment
  (require '[source.db.util :as db.util])
  (get {:ds (db.util/conn)})
  ())
