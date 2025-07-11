(ns source.routes.login
  (:require [source.services.auth :as auth]
            [ring.util.response :as res]
            [source.services.users :as users]
            [source.password :as pw]))

(defn post
  {:summary "get user data and access token provided valid credentials"
   :parameters {:body [:map
                       [:email :string]
                       [:password :string]]}
   :responses {200 {:body [:map
                           [:user
                            [:map
                             [:id :int]
                             [:address {:optional true} :string]
                             [:profile-image {:optional true} :string]
                             [:email :string]
                             [:firstname {:optional true} :string]
                             [:lastname {:optional true} :string]
                             [:type [:enum "creator" "distributor" "admin"]]
                             [:email-verified {:optional true} :int]
                             [:onboarded {:optional true} :int]
                             [:mobile {:optional true} :string]]]
                           [:access-token :string]
                           [:refresh-token :string]]}
               401 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]
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
