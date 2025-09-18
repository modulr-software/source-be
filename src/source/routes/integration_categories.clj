(ns source.routes.integration-categories
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all categories belonging to the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:bundle-id :int]
                            [:category-id :int]]]}}}

  [{:keys [ds path-params] :as _request}]
  (->> (services/categories-by-bundle ds {:bundle-id (:id path-params)})
       (res/response)))

(defn post
  {:summary "update categories belonging to the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]
                :body [:vector
                       [:map
                        [:id :int]
                        [:name :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (let [update-data (reduce (fn [acc {:keys [id]}]
                              (conj acc {:bundle-id (:id path-params)
                                         :category-id id})) [] body)]
    (services/delete-bundle-category! ds {:where [:= :integration-id (:id path-params)]})
    (services/insert-bundle-category! ds {:data update-data})
    (res/response {:message "successfully updated integration categories"})))
