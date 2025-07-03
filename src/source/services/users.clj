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

(defn insert-user! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :users}
       (merge opts)
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
  (insert-user! (db/ds :master) {:data {:email "chonkin@bonkin.com"
                                        :password (pw/hash-password "test")
                                        :firstname "merv"
                                        :lastname "vaneck"
                                        :type "creator"}
                                 :ret :*})
  (user (db/ds :master) {:id 5})
  (update-user! (db/ds :master) {:id 5
                                 :values {:firstname "kiigan"
                                          :lastname "korinzu"}})
  ())
