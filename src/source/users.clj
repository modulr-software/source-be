(ns source.users
  (:require [next.jdbc :as jdbc]
            [source.db :as db]
            [source.password :as pw]
            [source.util :as util]))

(defn create-user [user]
  (println "in user")
  (println user)
  (let [db (db/get-db) 
        username (:username user)
        password (:password user)]
    (jdbc/execute! db ["INSERT INTO users (username, password) VALUES (?, ?)", username, (pw/hash-password password)])))

(defn get-users []
  (let [db (db/get-db)]
    (mapv
     (fn [row]
       (->
        (util/unwrap-keys row)
        (dissoc :password)))
     (jdbc/execute! db ["SELECT * FROM USERS"]))))

(defn get-user-by-username [username]
  (let [db (db/get-db)]
    (-> (jdbc/execute! db ["SELECT * FROM USERS WHERE USERNAME = ?", username])
        (first)
        (util/unwrap-keys))))


(defn get-user-by-id [id]
  (let [db (db/get-db)]
    (-> (jdbc/execute! db ["SELECT * FROM USERS WHERE ID = ?", id])
        (first)
        (util/unwrap-keys))))

(defn delete-user [id]
  (let [db (db/get-db)]
    (jdbc/execute! db ["DELETE FROM USERS WHERE ID = ?", id])))
