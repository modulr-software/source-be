(ns source.routes.me
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get logged in user by access token"
   :responses {200 {:body [:map
                           [:id :int]
                           [:address {:optional true} :string]
                           [:profile-image {:optional true} :string]
                           [:email :string]
                           [:firstname {:optional true} :string]
                           [:lastname {:optional true} :string]
                           [:type [:enum "creator" "distributor" "admin"]]
                           [:email-verified {:optional true} :int]
                           [:onboarded {:optional true} :int]
                           [:mobile {:optional true} :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user] :as _request}]
  (let [user (->> user
                  (services/user ds))]
    (->> (dissoc user :password)
         (res/response))))
