(ns source.routes.integration-categories
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.db.util :as db.util]))

(defn get
  {:summary "get all categories belonging to the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}

  [{:keys [ds path-params] :as _request}]
  (with-open [bundle-ds (db.util/conn :bundle (:id path-params))]
    (let [category-ids (services/category-id-by-bundle bundle-ds {:bundle-id (:id path-params)})
          id-vec (mapv (fn [{:keys [category-id]}] category-id) category-ids)
          categories (services/categories ds {:where [:in :id id-vec]})]
      (res/response categories))))

(defn post
  {:summary "update categories belonging to the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]
                :body [:vector
                       [:map
                        [:id :int]
                        [:name :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [path-params body] :as _request}]
  (with-open [bundle-ds (db.util/conn :bundle (:id path-params))]
    (let [update-data (reduce (fn [acc {:keys [id]}]
                                (conj acc {:bundle-id (:id path-params)
                                           :category-id id})) [] body)]
      (services/delete-bundle-category! bundle-ds {:where [:= :bundle-id (:id path-params)]})
      (services/insert-bundle-category! bundle-ds {:data update-data})
      (res/response {:message "successfully updated integration categories"}))))
