(ns source.routes.me-sectors
  (:require [ring.util.response :as res]
            [source.services.user-sectors :as user-sectors]))

(defn get
  {:summary "get sectors for the logged-in user"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}
  [{:keys [ds user] :as _request}]
  (res/response (user-sectors/sectors-by-user ds {:user-id (:id user)})))

(defn post
  {:summary "update sectors for the logged-in user"
   :parameters {:body [:vector
                       [:map
                        [:id :int]
                        [:name :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}
  [{:keys [ds user body] :as _request}]
  (user-sectors/update-user-sectors! ds {:user-id (:id user)
                                         :sectors body})
  (res/response {:message "successfully updated user sectors"}))
