(ns source.db.master.connection
  (:require [source.db.util :as db.util]
            [source.config :as config]))

;; TODO:
;;-remove this file
;;-replace with utility that can generate the correct db-name
(def db-name "master")
(defn get-ds [] (db.util/conn db-name))

