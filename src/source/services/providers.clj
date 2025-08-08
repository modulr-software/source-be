(ns source.services.providers
  (:require [source.db.interface :as db]))

(defn insert-provider! [ds {:keys [data ret] :as opts}]
  (->> {:tname :providers
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn providers
  ([ds] (providers ds {}))
  ([ds opts]
   (->> {:tname :providers
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn provider [ds {:keys [id where] :as opts}]
  (->> {:tname :providers
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn delete-provider! [ds {:keys [id where] :as opts}]
  (->> {:tname :providers
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))
