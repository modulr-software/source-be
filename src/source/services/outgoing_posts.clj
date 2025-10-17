(ns source.services.outgoing-posts
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn outgoing-posts
  ([ds] (outgoing-posts ds {}))
  ([ds {:keys [where ret] :as opts}]
   (->> {:tname :outgoing-posts
         :where where
         :ret (or ret :*)}
        (merge opts)
        (db/find ds))))

(defn outgoing-post [ds {:keys [id where] :as opts}]
  (->> {:tname :outgoing-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn insert-outgoing-post! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :outgoing-posts}
       (merge opts)
       (db/insert! ds)))

(defn delete-outgoing-post! [ds {:keys [id where] :as opts}]
  (->> {:tname :outgoing-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))
