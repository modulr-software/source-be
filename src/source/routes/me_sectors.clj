(ns source.routes.me-sectors
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get sectors for the logged-in user"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}
  [{:keys [ds user] :as _request}]
  (res/response (services/sectors-by-user ds {:user-id (:id user)})))

(defn post
  {:summary "update sectors for the logged-in user"
   :parameters {:body [:vector
                       [:map
                        [:id :int]
                        [:name :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}
  [{:keys [ds user body] :as _request}]
  (let [update-data (reduce (fn [acc {:keys [id]}]
                              (conj acc {:user-id (:id user)
                                         :sector-id id})) [] body)]
    (services/delete-user-sector! ds {:where [:= :user-id (:id user)]})
    (services/insert-user-sector! ds {:data update-data})
    (res/response {:message "successfully updated user sectors"})))
