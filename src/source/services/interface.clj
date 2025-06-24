(ns source.services.interface
  (:require  [source.services.users :as users]
             [source.db.interface :as db]
             [source.services.auth :as auth]))

(defn users
  [& args]
  (apply users/users args))

(defn user [ds {:keys [_id] :as opts}]
  (users/user ds opts))

(defn insert-user! [ds {:keys [id] :as opts}]
  (users/insert-user! ds opts))

(defn login [ds {:keys [email] :as opts}]
  (auth/login ds opts))

(defn register [ds user]
  (auth/register ds user))

(comment
  (users (db/ds :master))
  (user (db/ds :master) {:id 1})
  (login (db/ds :master) {:email "merveillevaneck@gmail.com"})
  ())
