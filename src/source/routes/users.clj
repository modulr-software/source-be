(ns source.routes.users
  (:require [source.middleware.auth.core :as auth]
            [source.db.master.users :as db.users]
            [source.db.util :as db.util]
            [source.password :as pw]))

(defn login [req]
  (let [ds (db.util/conn :master)
        user (db.users/user-by
              ds
              {:col "email"
               :val (get-in req [:body :email])})
        password (get-in req [:body :password])]
    (cond
      (or (not (pw/verify-password password (:password user)))
          (not (some? user)))
      {:status 401 :body {:message "Invalid username or password!"}}

      :else
      (let [payload (dissoc user :password)]
        {:status 200
         :body (merge
                {:user payload}
                (auth/create-session payload))}))))

(defn register [req]
  (let [ds (db.util/conn :master)
        user (db.users/user-by
              ds
              {:col "email"
               :val (get-in req [:body :email])})
        {:keys [password confirm-password]} (:body req)]
    (cond
      (not (= password confirm-password))
      {:status 400 :body {:message "passwords do not match!"}}

      (some? user)
      {:status 400 :body {:message "an account for this email already exists!"}}

      :else
      (let [new-user (get-in req [:body])]
        (db.users/insert-user ds {:email (:email new-user)
                                  :password (pw/hash-password password)
                                  :sector-id 1
                                  :firstname (:firstname new-user)
                                  :lastname (:lastname new-user)
                                  :type (:type new-user)})
        {:status 200 :body {:message "successfully created user"}}))))

(defn users [_req]
  (let [ds (db.util/conn :master)]
    {:status 200
     :body {:users (db.users/users ds)}}))

(defn update-user [req]
  (let [user-id (get-in req [:params :id])
        cols (mapv name (keys (:body req)))
        values (vec (vals (:body req)))
        ds (db.util/conn :master)]

    (db.users/update-user! ds {:id user-id
                               :cols cols
                               :vals values})
    {:status 200
     :body {:message "successfully updated user"}}))

