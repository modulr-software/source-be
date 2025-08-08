(ns source.services.content-types
  (:require [source.db.interface :as db]))

(defn add-content-type! [ds {:keys [data ret] :as opts}]
  (->> {:tname :providers
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn content-types
  ([ds] (content-types ds {}))
  ([ds opts]
   (->> {:tname :content-types
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn content-type [ds {:keys [id where] :as opts}]
  (->> {:tname :providers
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
