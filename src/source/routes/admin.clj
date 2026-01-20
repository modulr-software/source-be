(ns source.routes.admin
  (:require [source.services.users :as users]
            [source.password :as pw]
            [ring.util.response :as res]))

(defn post
  {:summary "registers an admin user"
   :parameters {:body [:map
                       [:email :string]
                       [:password :string]
                       [:confirm-password :string]]}
   :responses {201 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]

  (let [user (users/user ds {:where [:= :email (:email body)]})
        {:keys [password confirm-password]} body]
    (cond
      (not (= password confirm-password))
      (-> (res/response {:message "passwords do not match!"})
          (res/status 400))

      (some? user)
      (-> (res/response {:message "an account for this email already exists!"})
          (res/status 400))

      :else
      (let [pw (pw/hash-password password)
            new-user (-> (assoc body
                                :password pw
                                :type "admin")
                         (dissoc :confirm-password))]
        (users/insert-user! ds {:data new-user})
        (res/response {:message "successfully created user"})))))
