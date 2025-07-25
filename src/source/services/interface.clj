(ns source.services.interface
  (:require  [source.services.users :as users]
             [source.db.interface :as db]
             [source.services.auth :as auth]
             [source.services.bundles :as bundles]))

(defn users
  [& args]
  (apply users/users args))

(defn user [ds {:keys [_id] :as opts}]
  (users/user ds opts))

(defn insert-user! [ds {:keys [_values _ret] :as opts}]
  (users/insert-user! ds opts))

(defn update-user! [ds {:keys [_id _values _where] :as opts}]
  (users/update-user! ds opts))

(defn login [ds {:keys [_email] :as opts}]
  (auth/login ds opts))

(defn register [ds user]
  (auth/register ds user))

(defn bundle [ds {:keys [_id _where] :as opts}]
  (bundles/bundle ds opts))

(comment
  (users (db/ds :master))
  (user (db/ds :master) {:id 2})
  (login (db/ds :master) {:email "merveillevaneck@gmail.com"})
  ())
