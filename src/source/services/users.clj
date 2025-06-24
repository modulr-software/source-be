(ns source.services.users
  (:require [source.db.interface :as db]
            [source.password :as pw]))

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

(defn insert-user! [ds user]
  (->> {:tname :users
        :data user}
       (db/insert! ds)))

(defn delete-user! [ds {:keys [id where] :as opts}]
  (->> {:tname :users
        :where (if (some? id) [:= :id id] where)}
       (merge opts)
       (db/delete! ds)))

(defn update-user! [ds {:keys [id values where] :as opts}]
  (->> {:tname :users
        :values values
        :where (if (some? id) [:= :id id] where)}
       (merge opts)
       (db/update! ds)))

(comment
  (users (db/ds :master))
  (insert-user! (db/ds :master) {:email "merveillevaneck@gmail.com"
                                 :password (pw/hash-password "test")
                                 :sector-id 1
                                 :firstname "merv"
                                 :lastname "vaneck"
                                 :type "admin"})
  (user (db/ds :master) {:id 5})
  (update-user! (db/ds :master) {:id 5
                                 :values {:firstname "kiigan"
                                          :lastname "korinzu"}})
  ())
