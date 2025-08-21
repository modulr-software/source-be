(ns source.services.baselines
  (:require [source.db.interface :as db]))

(defn insert-baseline! [ds {:keys [data ret] :as opts}]
  (->> {:tname :baselines
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn baselines
  ([ds] (baselines ds {}))
  ([ds opts]
   (->> {:tname :baselines
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn baseline [ds {:keys [id where] :as opts}]
  (->> {:tname :baselines
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
