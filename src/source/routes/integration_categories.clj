(ns source.routes.integration-categories
  (:require [ring.util.response :as res]
            [source.services.bundles :as bundles]
            [source.services.bundle-categories :as bundle-categories]))

(defn get
  {:summary "Get all categories belonging to the given integration by ID"
   :description "This endpoint pulls all the categories on which post selection is based."
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}

  [{:keys [ds path-params] :as _request}]
  (res/response (bundles/categories-in-bundle ds (:id path-params))))

(defn post
  {:summary "Update categories belonging to the given integration by ID"
   :description "This endpoint updates the list of categories for the given integration on which post selection is based; however, post selection will not immediately be rerun. Post selection will be rerun 24 hours from the previous execution. If you want post selection to be rerun immediately upon update, please use `POST: /integrations{id}`."
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]
                :body [:vector
                       [:map
                        [:id :int]
                        [:name :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (bundle-categories/update-bundle-categories! ds {:bundle-id (:id path-params)
                                                   :categories body})
  (res/response {:message "successfully updated integration categories"}))
