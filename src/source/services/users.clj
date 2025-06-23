(ns source.services.users
  (:require [source.db.interface :as db]))

(defn users
  ([ds] (users ds {}))
  ([ds opts]
   (->> {:tname :users}
        (merge opts)
        (db/find ds)
        (mapv #(dissoc % :password)))))

(defn user [ds {:keys [id where] :as opts}]
  (->> {:tname :users
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(comment
  (users (db/ds :master))
  ())
