(ns source.routes.me
  (:require [ring.util.response :as res]
            [source.util :as util]
            [source.db.honey :as hon]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [congest.jobs :as congest]
            [source.workers.users :as users]
            [source.email.gmail :as gmail]
            [source.email.templates :as templates]
            [source.routes.openapi :as api]
            [source.password :as password]))

(defn get
  {:summary "get logged in user by access token"
   :responses {200 {:body [:map
                           [:id :int]
                           [:address [:maybe :string]]
                           [:profile-image [:maybe :string]]
                           [:email :string]
                           [:firstname [:maybe :string]]
                           [:lastname [:maybe :string]]
                           [:type [:enum "creator" "distributor" "admin"]]
                           [:email-verified [:maybe :int]]
                           [:onboarded [:maybe :int]]
                           [:mobile [:maybe :string]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user] :as _request}]
  (let [user (hon/find-one ds {:tname :users
                               :where [:= :id (:id user)]})]
    (->> (dissoc user :password :email-hash)
         (res/response))))

(defn post
  {:summary "update logged-in user by access token"
   :parameters {:body [:map
                       [:address {:optional true} [:maybe :string]]
                       [:profile-image {:optional true} [:maybe :string]]
                       [:firstname {:optional true} :string]
                       [:lastname {:optional true} :string]
                       [:email-verified {:optional true} :int]
                       [:onboarded {:optional true} :int]
                       [:mobile {:optional true} [:maybe :string]]]}
   :responses {200 {:body [:map [:message :string]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user body] :as _request}]
  (hon/update! ds {:tname :users
                   :where [:= :id (:id user)]
                   :data body})
  (res/response {:message "successfully updated user"}))

(defn delete-user
  {:summary "delete logged-in user by access token"
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds js user] :as _request}]
  (let [{:keys [id type]} user
        job-id (handlers/user-deletion-job-id type id)]
    (users/soft-delete-user! ds js id)

    ; TODO: service needed
    (->> (jobs/prepare-congest-metadata
          ds
          {:id job-id
           :initial-delay (* 1000 60 60 24 24)
           :auto-start true
           :stop-after-fail false,
           :interval (* 1000 60 60 24 24)
           :recurring? false
           :kill-after 1
           :args {:user-type type
                  :user-id id}
           :handler :delete-user
           :created-at (util/get-utc-timestamp-string)
           :sleep false})
         (congest/register! js))
    (res/response {:message "successfully scheduled user deletion"})))

(defn cancel-deletion
  {:summary "cancel deletion of logged-in user by access token"
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds js user] :as _request}]
  (let [{:keys [id type]} user
        job-id (handlers/user-deletion-job-id type id)]
    (users/cancel-soft-user-deletion! ds id)
    (congest/deregister! js job-id)
    (res/response {:message "successfully cancelled user deletion"})))

(defn resend-email
  {:summary "Resend verification email"
   :responses (api/success (api/response-schema))}
  [{:keys [ds user]}]
  (let [{:keys [email email-hash]} (hon/find-one ds {:tname :users
                                                     :where [:= :id (:id user)]})]
    (gmail/send-email {:to email
                       :subject "Source - Verify your email"
                       :body (templates/email-verification {:email-hash email-hash})
                       :type :text/html})
    (res/response {:message "successfully resent email-verification email"})))

(defn password-reset-auth
  {:summary "Request an email to authenticate a password reset"
   :responses (api/success (api/response-schema))}
  [{:keys [ds user] :as _req}]
  (let [{:keys [email]} (hon/find-one ds {:tname :users
                                          :where [:= :id (:id user)]})
        password-hash (util/uuid)]
    (hon/update! ds {:tname :users
                     :where [:= :id (:id user)]
                     :data {:password-hash password-hash}})
    (gmail/send-email {:to email
                       :subject "Source - Reset your password"
                       :body (templates/password-reset-link {:password-hash password-hash})
                       :type :text/html})
    (res/response {:message "password reset email has been sent successfully"})))

(defn reset-password
  {:summary "Reset your password provided a valid password reset hash and the new password"
   :parameters (api/params :path [:map [:hash {:description "Password Reset Hash"} :string]]
                           :body api/PasswordResetParams)
   :responses (merge
               (api/success (api/response-schema))
               (api/unauthenticated nil))}
  [{:keys [ds path-params body]}]
  (let [{:keys [password confirm-password]} body
        user (users/user-by-password-hash ds (:hash path-params))]
    (if (and (some? user)
             (= password confirm-password))
      (do
        (hon/update! ds {:tname :users
                         :where [:= :password-hash (:hash path-params)]
                         :data {:password (password/hash-password password)
                                :password-hash nil}})
        (res/response {:message "successfully reset password"}))
      (res/response {:message "unauthorized"}))))

(defn verify-password-reset-hash
  {:summary "Verify password reset hash"
   :parameters (api/params :path [:map [:hash {:description "Password Reset Hash"} :string]])
   :responses (merge
               (api/success (api/response-schema))
               (api/unauthenticated nil))}
  [{:keys [ds path-params] :as _req}]
  (let [user (users/user-by-password-hash ds (:hash path-params))]
    (if (some? user)
      (res/response {:message "successfully verified password hash"})
      (res/response {:message "unauthorized"}))))
