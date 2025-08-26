(ns source.datastore.interface
  (:require [source.datastore.datahike :as dh]
            [source.datastore.util :as store.util]))

(defn ds [store-name]
  (store.util/conn store-name))

(defn entities [ds eids]
  (dh/entities ds eids))

(defn find [ds {:keys [_key _value] :as opts}]
  (dh/find ds opts))

(defn find-entities [ds {:keys [_key _value] :as opts}]
  (dh/find-entities ds opts))

(defn lookup [ds {:keys [_key _value] :as opts}]
  (dh/lookup ds opts))

(defn insert! [ds data]
  (dh/insert! ds data))

(defn update! [ds eid data]
  (dh/update! ds eid data))

(defn get-all [ds]
  (dh/get-all ds))

(defn delete! [ds eids]
  (dh/delete! ds eids))

(defn entries [ds k]
  (dh/entries ds k))

(defn entities-with [ds k]
  (dh/entities-with ds k))
