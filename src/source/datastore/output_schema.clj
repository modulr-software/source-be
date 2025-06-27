(ns source.datastore.output-schema
  (:require [source.datastore.datalevin :as dl]))

(defn get-value
  "Finds a value in the `output-schema` table by key."
  [ds key]
  (dl/get-value ds {:tname   :output-schema
                    :key     key}))

(defn delete!
  "Deletes one or more keys from the `output-schema` table."
  [ds keys]
  (dl/delete! ds {:tname :output-schema
                  :data   keys}))

(defn put!
  "Puts one or more key-value pairs into `output-schema`."
  [ds items]
  (dl/put! ds {:tname :output-schema
               :data  items}))

(defn get-all
  [ds]
  (dl/get-all ds {:tname :output-schema}))
