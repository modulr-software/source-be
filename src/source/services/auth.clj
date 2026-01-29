(ns source.services.auth
  (:require [source.password :as pw]
            [source.middleware.auth.core :as auth]
            [source.db.honey :as hon]))

(defn login [ds {:keys [user] :as _login}]
  (merge
   {:user (dissoc user :password)}
   (auth/create-session (select-keys user [:id :type]))))

(defn register [ds {:keys [email password] :as user}]
  (hon/insert! ds {:tname :users
                   :data (-> user
                             (dissoc :confirm-password)
                             (assoc :password (pw/hash-password password)))})
  (let [user (hon/find-one ds {:tname :users
                               :where [:= :email email]})]
    (merge
     {:user (dissoc user :password)}
     (auth/create-session (select-keys user [:id :type])))))

(comment
  (require '[source.db.interface :as db])
  (login (db/ds :master) {:user {:email "merveillevaneck@gmail.com" :type "admin"}})
  ())
