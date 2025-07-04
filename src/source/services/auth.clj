(ns source.services.auth
  (:require [source.password :as pw]
            [source.services.users :as users]
            [source.middleware.auth.core :as auth]))

(defn login [ds {:keys [user] :as _login}]
  (merge
   {:user (dissoc user :password)}
   (auth/create-session (select-keys user [:id :type]))))

(defn register [ds {:keys [email password] :as user}]
  (users/insert-user! ds {:data (-> user
                                    (dissoc :confirm-password)
                                    (assoc :password (pw/hash-password password)))})
  (let [user (users/user ds {:where [:= :email email]})]
    (merge
     {:user user}
     (auth/create-session (select-keys user [:id :type])))))

(comment
  (require '[source.db.interface :as db])
  (login (db/ds :master) {:user {:email "merveillevaneck@gmail.com" :type "admin"}})
  ())
