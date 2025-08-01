(ns source.services.providers
  (:require [source.db.interface :as db]
            [source.datastore.interface :as store]))

(defn insert-provider! [ds provider]
  (->> {:tname :providers
        :data provider}
       (db/insert! ds)))

(defn providers
  ([ds] (providers ds {}))
  ([ds opts]
   (->> {:tname :providers}
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

