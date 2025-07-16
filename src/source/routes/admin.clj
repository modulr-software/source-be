(ns source.routes.admin
  (:require [source.services.users :as users]
            [source.password :as pw]
            [source.util :as util]
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

  (let [{:keys [data error success]} (util/validate post body)]
    (if (not success)

      (-> (res/response error)
          (res/status 400))

      (let [user (users/user
                  ds
                  {:where [:= :email (:email data)]})
            {:keys [password confirm-password]} data]
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
            {:status 200 :body {:message "successfully created user"}}))))))
