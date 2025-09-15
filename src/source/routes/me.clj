(ns source.routes.me
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.util :as util]))

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
                       [:address [:maybe :string]]
                       [:profile-image [:maybe :string]]
                       [:firstname [:maybe :string]]
                       [:lastname [:maybe :string]]
                       [:email-verified [:maybe :int]]
                       [:onboarded [:maybe :int]]
                       [:mobile [:maybe :string]]]}
   :responses {200 {:body [:map [:message :string]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user body] :as _request}]
  (let [{:keys [data error success]} (util/validate post body)]
    (if (not success)
      (-> (res/response {:message error})
          (res/status 400))
      (services/update-user! ds {:id (:id user)
                                 :data data}))))
