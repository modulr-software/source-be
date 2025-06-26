(ns source.routes.admin
  (:require [source.services.users :as users]
            [source.db.util :as db.util]
            [source.password :as pw]))

(defn post [request]
  (let [ds (db.util/conn :master)
        user (users/user
              ds
              {:where [:= :email (get-in request [:body :email])]})
        {:keys [password confirm-password]} (:body request)]
    (cond
      (not (= password confirm-password))
      {:status 400 :body {:message "passwords do not match!"}}

      (some? user)
      {:status 400 :body {:message "an account for this email already exists!"}}

      :else
      (let [new-user (get-in request [:body])]
        (users/insert-user! ds {:email (:email new-user)
                                :password (pw/hash-password password)
                                :firstname (:firstname new-user)
                                :lastname (:lastname new-user)
                                :type "admin"})
        {:status 200 :body {:message "successfully created user"}}))))

