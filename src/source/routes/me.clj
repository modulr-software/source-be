(ns source.routes.me
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

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
