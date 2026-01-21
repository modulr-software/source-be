(ns source.routes.login
  (:require [source.services.auth :as auth]
            [ring.util.response :as res]
            [source.services.users :as users]
            [source.password :as pw]
            [source.util :as util]))

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

  (let [{:keys [data error success]} (util/validate post body)
        {:keys [email password]} data
        user (users/user ds {:where [:= :email email]})]

    (cond
      (not success) (-> (res/response error)
                        (res/status 400))

      (or (not (some? user))
          (not (pw/verify-password password (:password user))))
      {:status 401 :body {:message "Invalid username or password!"}}

      :else (res/response (auth/login ds {:user user})))))

(comment
  (require '[source.db.interface :as db])
  (post {:ds (db/ds :master) :body {:email "toast@toast.com" :password "poop"}})
  ())
