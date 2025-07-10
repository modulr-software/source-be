(ns source.routes.users
  (:require [ring.util.response :as res]
            [source.services.users :as users]))

(defn get [{:keys [ds] :as _request}]
  (res/response {:users (users/users ds)}))

(def get-responses {200 {:body [:map
                                    [:users
                                     [:vector
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
                                       [:mobile {:optional true} :string]]]]]}
                        401 {:body [:map [:message :string]]}
                        403 {:body [:map [:message :string]]}})

(comment
  (require '[source.db.interface :as db])
  (get {:ds (db/ds :master)})
  ())
