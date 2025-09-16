(ns source.services.integrations
  (:require [source.db.interface :as db]))

(defn insert-integration! [ds {:keys [data] :as opts}]
  (->> {:tname :integrations
        :data data
        :ret :1}
       (merge opts)
       (db/insert! ds)))

(defn update-integration! [ds {:keys [id data where] :as opts}]
  (->> {:tname :integrations
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn integrations
  ([ds] (integrations ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :feeds
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn integration [ds {:keys [id where] :as opts}]
  (->> {:tname :integrations
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
