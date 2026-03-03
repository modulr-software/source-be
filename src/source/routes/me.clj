(ns source.routes.me
  (:require [ring.util.response :as res]
            [source.util :as util]
            [source.db.honey :as hon]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [congest.jobs :as congest]
            [source.workers.users :as users]
            [source.email.gmail :as gmail]
            [source.email.templates :as templates]))

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
    (->> (dissoc user :password)
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
    (users/soft-delete-user! ds id)

    ; TODO: service needed
    (->> (jobs/prepare-congest-metadata
          ds
          {:id job-id
           :initial-delay (* 1000 60 60 24 30)
           :auto-start true
           :stop-after-fail false,
           :interval (* 1000 60 60 24 30)
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
  {:summary "Resend verification email"}
  [{:keys [ds user]}]
  (let [{:keys [email email-hash]} (hon/find-one ds {:tname :users
                                                     :where [:= :id (:id user)]})]
    (gmail/send-email {:to email
                       :subject "Source - Verify your email"
                       :body (templates/email-verification email-hash)
                       :type :text/html})))
