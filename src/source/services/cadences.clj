(ns source.services.cadences
  (:require [source.db.interface :as db]))

(defn insert-cadence! [ds {:keys [data ret] :as opts}]
  (->> {:tname :cadences
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn cadences
  ([ds] (cadences ds {}))
  ([ds opts]
   (->> {:tname :cadences
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn cadence [ds {:keys [id where] :as opts}]
  (->> {:tname :cadences
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
