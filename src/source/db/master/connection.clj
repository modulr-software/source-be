(ns source.db.master.connection
  (:require [source.db.util :as db.util]))

(def db-name "master")
(def ds (db.util/conn db-name))