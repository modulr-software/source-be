(ns source.routes.users
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

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

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master)})
  ())
