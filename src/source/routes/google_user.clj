(ns source.routes.google-user
  (:require [source.oauth2.google.interface :as google]
            [source.middleware.auth.core :as auth]
            [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.password :as pw]
            [source.email.templates :as templates]
            [source.email.gmail :as gmail]))

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
        _ (prn "email from google" email)
        user (hon/find-one ds {:tname :users
                               :where [:= :email email]})
        _ (prn "try find user, got:" user)
        user-type (get-in req [:cookies "user_type" :value])
        _ (prn "user-type" user-type)]

    (if (some? user)
      (let [payload (dissoc user :password)
            session (auth/create-session payload)]
        (prn "found a user, sending response")
        (res/response (merge {:user payload} session)))
      (do
        (prn "no user, inserting user with email hash" email (pw/hash-password email) user-type)
        (hon/insert! ds {:tname :users
                         :data {:email email
                                :email-hash (pw/hash-password email)
                                :type user-type}})
        (prn "inserted user")
        (let [new-user (hon/find-one ds {:tname :users
                                         :where [:= :email email]})
              _ (prn "retrieved new user" new-user)
              payload (dissoc new-user :password)
              _ (prn "user without password" payload)
              session (auth/create-session payload)]
          (prn "session" session)
          (prn "going to send email with" (pw/hash-password email) "to" email)
          (gmail/send-email {:to email
                             :subject "Source - Verify your email"
                             :body (templates/email-verification {:email-hash (pw/hash-password email)})
                             :type :text/html})
          (prn "sent email with" (pw/hash-password email) "to" email)
          (res/response (merge {:user payload} session)))))))

(comment
  (require '[source.db.util :as db.util])

  (def ds (db.util/conn))
  (hon/insert! ds {:tname :users
                   :data {:email "merv@simply.co.za"
                          :email-hash (pw/hash-password "merv@simply.co.za")
                          :type "creator"}})
  (hon/find-one ds {:tname :users
                    :where [:= :email "merv@simply.co.za"]})
  (let [new-user (hon/find-one ds {:tname :users
                                   :where [:= :email "merv@simply.co.za"]})
        payload (dissoc new-user :password)
        session (auth/create-session payload)]
    #_(gmail/send-email {:to email
                         :subject "Source - Verify your email"
                         :body (templates/email-verification {:email-hash (pw/hash-password email)})
                         :type :text/html})
    (res/response (merge {:user payload} session)))
  ())
