(ns source.routes.admin
  (:require [source.services.users :as users]
            [source.db.util :as db.util]
            [source.password :as pw]))

(defn post
  {:summary "registers an admin user"
   :parameters {:body [:map
                       [:email :string]
                       [:password :string]
                       [:confirm-password :string]]}
   :responses {201 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [body] :as _request}]
  (let [ds (db.util/conn :master)
        user (users/user
              ds
              {:where [:= :email (:email body)]})
        {:keys [password confirm-password]} body]
    (cond
      (not (= password confirm-password))
      {:status 400 :body {:message "passwords do not match!"}}

      (some? user)
      {:status 400 :body {:message "an account for this email already exists!"}}

      :else
      (let [pw (pw/hash-password password)
            new-user (-> (assoc body
                                :password pw
                                :type "admin")
                         (dissoc :confirm-password))]
        (users/insert-user! ds {:data new-user})
        {:status 200 :body {:message "successfully created user"}}))))
