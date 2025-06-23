(ns source.routes.login
  (:require [source.services.login :as login]))

(defn login [{:keys [ds] :as _request}]
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
