(ns source.services.xml
  (:require [source.db.interface :as db]
            [source.datastore.datalevin :as dl]))

(defn get-all
  [ds {:keys [tname]}]
  (dl/get-all ds {:tname tname}))

(defn add-output-schema!
  [ds {:keys [data]}]
  (dl/put! ds {:tname :output-schema
               :data data}))

(defn insert-selection-schema!
  [ds]
  (db/insert! ds {:tname :selection-schemas}))
