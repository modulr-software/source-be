(ns source.services.categories
  (:require [source.db.interface :as db]))

(defn insert-category! [ds {:keys [data ret] :as opts}]
  (->> {:tname :categories
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn update-category! [ds {:keys [id data where] :as opts}]
  (->> {:tname :categories
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn categories
  ([ds] (categories ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :categories
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn category [ds {:keys [id where] :as opts}]
  (->> {:tname :categories
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(comment 
  (require '[source.db.util :as db.util])
  (def ds (db.util/conn))

  (categories ds)
  (insert-category! ds {:data {:name "programming"}})
  ())
