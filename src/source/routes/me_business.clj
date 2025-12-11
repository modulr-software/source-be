(ns source.routes.me-business
  (:require [source.util :as util]
            [ring.util.response :as res]
            [source.services.interface :as services]))

(defn get
  {:summary "get business for logged-in user"
   :responses {200 {:body [:map
                           [:id :int]
                           [:name [:maybe :string]]
                           [:address [:maybe :string]]
                           [:url [:maybe :string]]
                           [:linkedin [:maybe :string]]
                           [:twitter [:maybe :string]]
                           [:registration [:maybe :string]]
                           [:business-type-id {:optional true} :int]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user] :as _request}]
  (let [{:keys [business-id]} (services/user ds {:id (:id user)})]
    (res/response (services/business ds {:id business-id}))))

(defn post
  {:summary "add or update business for logged-in user"
   :parameters {:body [:map
                       [:name {:optional true} :string]
                       [:address {:optional true} :string]
                       [:url {:optional true} :string]
                       [:linkedin {:optional true} :string]
                       [:twitter {:optional true} :string]
                       [:registration {:optional true} :string]
                       [:business-type-id {:optional true} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user body] :as _request}]
  (let [{:keys [data error success]} (util/validate post body)]
    (if (not success)
      (-> (res/response {:message error})
          (res/status 400))

      (let [{:keys [business-id]} (services/user ds {:id (:id user)})
            business (when (nil? business-id)
                       (services/insert-business! ds {:data data
                                                      :ret :1}))]
        (if (nil? business-id)
          (services/update-user! ds {:id (:id user)
                                     :data {:business-id (:id business)}})
          (services/update-business! ds {:id business-id
                                         :data data}))

        (res/response {:message "successfully added business"})))))
