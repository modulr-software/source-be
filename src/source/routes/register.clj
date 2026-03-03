(ns source.routes.register
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn post
  {:summary "register a new user"
   :parameters {:body [:map
                       [:email :string]
                       [:password :string]
                       [:confirm-password :string]
                       [:type [:enum "creator" "distributor"]]]}
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

  [{:keys [ds body] :as _request}]

  (let [{:keys [email password confirm-password]} body
        existing-user (hon/find-one ds {:tname :users
                                        :where [:= :email email]})]
    (cond
      (not (= password confirm-password))
      (-> (res/response {:error "Passwords do not match!"}))

      (some? existing-user)
      (-> (res/response {:error "An account for this email already exists!"}))

      :else
      (-> (services/register ds body)
          (res/response)))))

(comment
  (require '[source.db.interface :as db])
  (post {:ds (db/ds :master) :body {:email "poop@test.com"
                                    :password "test"
                                    :type "distributor"
                                    :confirm-password "test"}})
  ())

