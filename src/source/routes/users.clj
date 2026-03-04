(ns source.routes.users
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.routes.openapi :as api]))

(defn get
  {:summary "get all users"
   :responses  {200 {:body [:map
                            [:users
                             [:vector
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
                               [:mobile [:maybe :int]]]]]]}
                401 {:body [:map [:message :string]]}
                403 {:body [:map [:message :string]]}}}

  [{:keys [ds] :as _request}]
  (res/response {:users (hon/find ds {:tname :users
                                      :ret :*})}))

(defn verify-email
  {:summary "verify user email with email hash"
   :parameters (api/params :path [:map [:id :int]])
   :responses (api/success (api/response-schema))}
  [{:keys [ds path-params]}]
  (hon/update! ds {:tname :users
                   :where [:= :id (:id path-params)]
                   :data {:email-verified 1
                          :email-hash ""}})
  (res/response {:message "successfully verified user"}))

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master)})
  ())
