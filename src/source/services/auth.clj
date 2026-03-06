(ns source.services.auth
  (:require [source.password :as pw]
            [source.middleware.auth.core :as auth]
            [source.db.honey :as hon]
            [source.email.gmail :as gmail]
            [source.email.templates :as templates]))

(defn login [ds {:keys [user] :as _login}]
  (merge
   {:user (dissoc user :password :email-hash)}
   (auth/create-session (select-keys user [:id :type]))))

(defn register [ds {:keys [email password] :as user}]
  (hon/insert! ds {:tname :users
                   :data (-> user
                             (dissoc :confirm-password)
                             (assoc :password (pw/hash-password password)
                                    :email-hash (pw/hash-password email)))})
  (gmail/send-email {:to email
                     :subject "Source - Verify your email"
                     :body (templates/email-verification {:email-hash (pw/hash-password email)})
                     :type :text/html})
  (let [user (hon/find-one ds {:tname :users
                               :where [:= :email email]})]
    (merge
     {:user (dissoc user :password :email-hash)}
     (auth/create-session (select-keys user [:id :type])))))

(comment
  (require '[source.db.interface :as db])
  (login (db/ds :master) {:user {:email "merveillevaneck@gmail.com" :type "admin"}})
  ())
