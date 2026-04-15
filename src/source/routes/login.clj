(ns source.routes.login
  (:require [source.services.auth :as auth]
            [ring.util.response :as res]
            [source.password :as pw]
            [source.db.honey :as hon]))

(defn post
  {:summary "get user data and access token provided valid credentials"
   :parameters {:body [:map
                       [:email :string]
                       [:password :string]]}
   :responses {200 {:body [:map
                           [:user
                            [:map
                             [:id :int]
                             [:address [:maybe :string]]
                             [:profile-image [:maybe :string]]
                             [:email :string]
                             [:firstname [:maybe :string]]
                             [:lastname [:maybe :string]]
                             [:type [:enum "creator" "distributor" "admin"]]
                             [:email-verified [:maybe :int]]
                             [:onboarded [:maybe :int]]
                             [:mobile [:maybe :string]]]]
                           [:access-token :string]
                           [:refresh-token :string]]}
               401 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]

  (let [{:keys [email password]} body
        user (hon/find-one ds {:tname :users
                               :where [:= :email email]})]

    (if
     (or (not (some? user))
         (not (pw/verify-password password (:password user))))
      {:status 401 :body {:message "Invalid username or password!"}}

      (res/response (auth/login ds {:user user})))))

(comment
  (require '[source.db.interface :as db])
  (post {:ds (db/ds :master) :body {:email "toast@toast.com" :password "poop"}})
  ())
