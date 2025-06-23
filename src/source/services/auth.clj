(ns source.services.auth
  (:require [source.password :as pw]
            [source.db.interface :as db]
            [source.services.users :as users]
            [source.middleware.auth.core :as auth]))

(defn login [ds {:keys [email] :as _login}]
  (let [user (users/user ds {:where [:= :email email]})]
    (merge
     {:user (dissoc user :password)}
     (auth/create-session (select-keys user [:id :type])))))

(comment
  (require '[source.db.interface :as db])
  (login (db/ds :master) {:email "merveillevaneck@gmail.com" :type "admin"})
  ())
