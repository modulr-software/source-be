(ns source.routes.me-business
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

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
                           [:business-type-id [:maybe :int]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds user] :as _request}]
  (let [{:keys [business-id]} (hon/find-one ds {:tname :users
                                                :where [:= :id (:id user)]})
        business (if business-id
                   (hon/find-one ds {:tname :businesses
                                     :where [:= :id business-id]})
                   {})]
    (res/response business)))

(defn post
  {:summary "add or update business for logged-in user"
   :parameters {:body [:map
                       [:name {:optional true} [:maybe :string]]
                       [:address {:optional true} [:maybe :string]]
                       [:url {:optional true} [:maybe :string]]
                       [:linkedin {:optional true} [:maybe :string]]
                       [:twitter {:optional true} [:maybe :string]]
                       [:registration {:optional true} [:maybe :string]]
                       [:business-type-id {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:map [:message :string]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user body] :as _request}]
  (let [{:keys [business-id]} (hon/find-one ds {:tname :users
                                                :where [:= :id (:id user)]})
        business (when (nil? business-id)
                   (hon/insert! ds {:tname :businesses
                                    :data body
                                    :ret :1}))]
    (if (nil? business-id)
      (hon/update! ds {:tname :users
                       :where [:= :id (:id user)]
                       :data {:business-id (:id business)}})
      (hon/update! ds {:tname :businesses
                       :where  [:= :id business-id]
                       :data body}))

    (res/response {:message "successfully added or updated business"})))
