(ns source.routes.google-user
  (:require [source.oauth2.google.interface :as google]
            [source.middleware.auth.core :as auth]
            [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "completes the google oauth2 flow and returns the authenticated user"
   :parameters {:query [:map
                        [:code :string]
                        [:scope :string]]}
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
                           [:refresh-token :string]]}}}
  [{:keys [ds body] :as req}]

  (let [{:keys [uuid _uri]} body
        email (google/google-session-user uuid (:params req))
        user (hon/find-one ds {:tname :users
                               :where [:= :email email]})
        user-type (get-in req [:cookies "user_type" :value])]

    (if (some? user)
      (let [payload (dissoc user :password)
            session (auth/create-session payload)]
        (res/response (merge {:user payload} session)))
      (do
        (hon/insert! ds {:tname :users
                         :data {:email email
                                :type user-type}})
        (let [new-user (hon/find-one ds {:tname :users
                                         :where [:= :email email]})
              payload (dissoc new-user :password)
              session (auth/create-session payload)]
          (res/response (merge {:user payload} session)))))))
