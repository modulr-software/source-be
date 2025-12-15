(ns source.routes.me
  (:require [source.services.interface :as services]
            [source.routes.feed :as feed]
            [source.routes.integration :as integration]
            [ring.util.response :as res]
            [source.util :as util]
            [source.services.analytics.interface :as analytics]))

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
  (let [user (->> user
                  (services/user ds))]
    (->> (dissoc user :password)
         (res/response))))

(defn post
  {:summary "update logged-in user by access token"
   :parameters {:body [:map
                       [:address {:optional true} :string]
                       [:profile-image {:optional true} [:maybe :string]]
                       [:firstname {:optional true} :string]
                       [:lastname {:optional true} :string]
                       [:email-verified {:optional true} :int]
                       [:onboarded {:optional true} :int]
                       [:mobile {:optional true} :string]]}
   :responses {200 {:body [:map [:message :string]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user body] :as _request}]
  (let [{:keys [data error success]} (util/validate post body)]
    (if (not success)
      (-> (res/response {:message error})
          (res/status 400))
      (do (services/update-user! ds {:id (:id user)
                                     :data data})
          (res/response {:message "successfully updated user"})))))

(defn hard-delete-creator [ds js user-id email]
  (let [feed-ids (mapv :id (services/feeds ds {:where [:= :user-id user-id]}))]
    (run! #(feed/hard-delete-feed! ds js email %) feed-ids)
    (analytics/delete-event! ds {:where [:= :creator-id user-id]})))

(defn hard-delete-distributor [ds js user-id]
  (let [bundle-ids (mapv :id (services/bundles ds {:where [:= :user-id user-id]}))]
    (run! #(integration/hard-delete-bundle! ds js %) bundle-ids)
    (analytics/delete-event! ds {:where [:= :distributor-id user-id]})))

(defn hard-delete-user [user-type ds js user-id]
  (let [{:keys [email business-id]} (services/user ds {:id user-id})]
    (cond
      (= user-type :creator)
      (hard-delete-creator ds js user-id email)
      (= user-type :distributor)
      (hard-delete-distributor ds js user-id))

    (services/delete-user-sector! ds {:where [:= :user-id user-id]})
    (when (some? business-id) (services/delete-business! ds {:id business-id}))
    (services/delete-user! ds {:id user-id})))

(defn delete
  {:summary "delete logged-in user by access token"
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds js user] :as _request}]
  (let [{:keys [id type]} user]
    (hard-delete-user (keyword type) ds js id)
    (res/response {:message "successfully deleted user"})))
