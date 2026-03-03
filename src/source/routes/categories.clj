(ns source.routes.categories
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.workers.categories :as categories]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [malli.util :as mu]))

(defn get
  {:summary "get all categories"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}}}
  [{:keys [ds] :as _request}]
  (->> (hon/find ds {:tname :categories})
       (res/response)))

(defn used-categories
  {:summary "get all categories for which feeds and posts exist in the system"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}}}
  [{:keys [ds] :as _request}]
  (res/response (categories/used-categories ds)))

(defn add-category
  {:summary "add a new category to the system"
   :parameters (api/params :body (-> schemas/Category
                                     (mu/dissoc :id)))
   :responses (api/success (api/response-schema))}
  [{:keys [ds body]}]
  (hon/insert! ds {:tname :categories
                   :data body})
  (res/response {:message "successfully added new category"}))

(defn update-category
  {:summary "add a new category to the system"
   :parameters (api/params
                :path [:map [:id :int]]
                :body (-> schemas/Category
                          (mu/dissoc :id)))
   :responses (api/success (api/response-schema))}
  [{:keys [ds body path-params]}]
  (hon/update! ds {:tname :categories
                   :where [:= :id (:id path-params)]
                   :data body})
  (res/response {:message "successfully updated category"}))

(defn delete-category
  {:summary "delete a category from the system"
   :parameters (api/params :path [:map [:id :int]])
   :responses (api/success (api/response-schema))}
  [{:keys [ds path-params]}]
  (categories/delete-category! ds (:id path-params))
  (res/response {:message "successfully deleted category"}))
