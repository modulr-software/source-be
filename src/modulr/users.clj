(ns modulr.users
  (:require [modulr.db :as db]
            [next.jdbc :as jdbc]
            [modulr.password :as pw]))

(defn create-user [user]
  (println "in user")
  (println user)
  (let [db (db/get-db) 
        username (:username user)
        password (:password user)]
    (println username)
    (println password)
    (jdbc/execute! db ["INSERT INTO users (username, password) VALUES (?, ?)", username, (pw/hash-password password)])))


(defn get-users []
  (let [db (db/get-db)]
    (mapv
     (fn [row]
       (update-keys
        row
        (fn [k] (keyword (name k)))))
     (jdbc/execute! db ["SELECT * FROM USERS"]))
    ))

(defn delete-user [id]
  (let [db (db/get-db)]
    (jdbc/execute! db ["DELETE FROM USERS WHERE ID = ?", id])))

(comment
  (jdbc/execute! (db/get-db) ["SELECT * FROM USERS"])
  (jdbc/execute! (db/get-db) ["DROP TABLE USERS"])
  (jdbc/execute! (db/get-db) db/scaffold-query)
  (create-user {:username "test" :password "test"})
  )

(get-users)