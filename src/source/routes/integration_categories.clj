(ns source.routes.integration-categories
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.db.util :as db.util]
            [source.services.bundles :as bundles]
            [source.services.bundle-categories :as bundle-categories]))

(defn get
  {:summary "get all categories belonging to the integration with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "integration id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}

  [{:keys [ds path-params] :as _request}]
  (res/response (bundles/categories-in-bundle ds (:id path-params))))

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
  (let [bundle-ds (db.util/conn :bundle (:id path-params))]
    (bundle-categories/update-bundle-categories! bundle-ds {:bundle-id (:id path-params)
                                                            :categories body})
    (res/response {:message "successfully updated integration categories"})))
