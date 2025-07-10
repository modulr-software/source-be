(ns source.routes.register
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post [{:keys [ds body] :as _request}]
  ;;TODO: needs schema validation here
  (let [{:keys [email password confirm-password]} body
        existing-user (services/user ds {:where [:= :email email]})]
    (cond
      (not (= password confirm-password))
      (-> (res/response {:error "Passwords do not match!"}))

      (some? existing-user)
      (-> (res/response {:error "An account for this email already exists!"}))

      :else
      (-> (services/register ds body)
          (res/response)))))

(def post-parameters {:body [:map
                             [:email :string]
                             [:password :string]
                             [:confirm-password :string]]})

(def post-responses {200 {:body [:map
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
                                 [:refresh-token :string]]}})

(comment
  (require '[source.db.interface :as db])
  (post {:ds (db/ds :master) :body {:email "test@test.com"
                                    :password "test"
                                    :type "distributor"
                                    :confirm-password "test"}})
  ())
