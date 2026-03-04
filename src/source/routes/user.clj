(ns source.routes.user
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.routes.openapi :as api]
            [source.config :as conf]))

(defn get
  {:summary "get user by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "user id"} :int]]}
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
                             [:mobile [:maybe :string]]]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (let [user (hon/find-one ds {:tname :users
                               :where [:= :id (:id path-params)]})]
    (->> (dissoc user :password)
         (assoc {} :user)
         (res/response))))

(defn patch
  {:summary "update user by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "user id"} :int]]
                :body [:map
                       [:address {:optional true} :string]
                       [:profile-image {:optional true} :string]
                       [:email :string]
                       [:firstname {:optional true} :string]
                       [:lastname {:optional true} :string]
                       [:type [:enum "creator" "distributor" "admin"]]
                       [:email-verified {:optional true} :int]
                       [:onboarded {:optional true} :int]
                       [:mobile {:optional true} :string]]}
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
                             [:mobile [:maybe :string]]]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds body path-params] :as _request}]
  (hon/update! ds {:tname :users
                   :where [:= :id (:id path-params)]
                   :data body})
  (res/response {:message "successfully updated user"}))

(defn verify-email
  {:summary "verify user email with email hash"
   :parameters (api/params :path [:map [:hash :string]])
   :responses {302 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}
  [{:keys [ds path-params]}]
  (let [email-hash (:hash path-params)
        user (hon/find-one ds {:tname :users
                               :where [:= :email-hash email-hash]})]
    (if (some? user)
      (do
        (hon/update! ds {:tname :users
                         :where [:= :id (:id user)]
                         :data {:email-verified 1
                                :email-hash ""}})
        (-> (conf/read-value :cors-origin)
            (str "/dashboard/onboarding")
            (res/redirect)))
      (-> (res/response {:message "unauthorized"})
          (res/status 403)))))

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master) :path-params {:id 5}})
  (patch {:ds (db/ds :master)
          :path-params {:id 5}
          :body {:firstname "kiigan"
                 :lastname "korinzu"}})

  (meta #'get)
  ())
