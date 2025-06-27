(ns source.datastore.selection-schema
  (:require [source.datastore.datalevin :as dl]))

(defn get-value
  "Finds a value in the `selection-schema` table by key."
  [ds key]
  (dl/get-value ds {:tname   :selection-schema
                    :key     key}))

(defn delete!
  "Deletes one or more keys from the `selection-schema` table."
  [ds keys]
  (dl/delete! ds {:tname :selection-schema
                  :data   keys}))

(defn put!
  "Puts one or more key-value pairs into `selection-schema`."
  [ds items]
  (dl/put! ds {:tname :selection-schema
               :data  items}))

(defn get-all
  [ds]
  (dl/get-all ds {:tname :selection-schema}))
