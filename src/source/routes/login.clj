(ns source.routes.login
  (:require [source.services.auth :as auth]
            [ring.util.response :as res]
            [source.services.users :as users]
            [source.password :as pw]))

(defn post [{:keys [ds body] :as _request}]
  (let [{:keys [email password]} body
        user (users/user ds {:where [:= :email email]})]
    (if
     (or (not (pw/verify-password password (:password user)))
         (not (some? user)))
      {:status 401 :body {:message "Invalid username or password!"}}

      (res/response (auth/login ds {:user user})))))

(comment
  (require '[source.db.interface :as db])
  (post {:ds (db/ds :master) :body {:email "toast@toast.com" :password "poop"}})
  ())
