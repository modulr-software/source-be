(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [jsonista.core :as json]
            [source.db.master.core :as master]
            [malli.generator :as mg]
            [source.db.bundle.outgoing-posts :as oposts]
            [source.db.master.feeds-categories :as feeds-categories]))

(def ^:private sqlite-config
  {:dbtype "sqlite"})

(defn db-path [dbname]
  (str (conf/read-value :database-dir) dbname))

;; TODO:
;; - introduce utility to generate dynamic db names

(defn conn [dbname]
  (-> sqlite-config
      (merge {:dbname (db-path (name dbname))})
      (jdbc/get-connection)
      (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))

(defn creator-id
  ([post-id]
   (-> (conn :master)
       (creator-id post-id)))
  ([ds post-id]
   (-> (oposts/select-outgoing-post-by-id ds {:id post-id})
       (:creator_id))))

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(defn generate-items [schema count]
  (mg/generate [:vector {:gen/min count :gen/max count} schema]))

(defn seed-data [path type]
  (-> (slurp (or path (str "resources/" (name type) ".json")))
      (json/read-value)))

(defn column-names [data]
  (->> (mapv (fn [item] (mapv (fn [[key _]] (name key)) item)) data)
       (flatten)
       (apply hash-set)
       (vec)))

(defn records [columns data]
  (mapv (fn [item] (mapv (fn [col] (get item (name col))) columns)) data))

(defn apply-seed [ds tname data]
  (let [cols (column-names data)
        vals (records cols data)]
    (master/seed-table ds {:table tname
                           :cols cols
                           :vals vals})))

(defn get-post-categories [bundle-ds ds post-id]
  (let [feed-id (-> (oposts/select-outgoing-post-by-id bundle-ds {:id post-id})
                    (:feed_id))]
    (feeds-categories/select-by-feed-id ds {:feed-id feed-id})))
