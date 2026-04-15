(ns source.routes.business-types
  (:require [source.db.honey :as db]
            [ring.util.response :as res]))

(defn get
  {:summary "Get all business types"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}
  [{:keys [ds] :as _request}]
  (res/response (db/find ds {:tname :business-types
                             :ret :*})))

(defn post
  {:summary "add one or more new business types"
   :parameters {:body [:vector
                       [:map
                        [:name :string]]]}
   :responses {201 {:body [:map [:message :string]]}}}

  [{:keys [ds body] :as _request}]
  (db/insert! ds {:tname :business-types
                  :data body})
  (res/response {:message "successfully added business type(s)"}))

(defn patch
  {:summary "update business type with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "business type id"} :int]]
                :body [:map [:name :string]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (db/update! ds {:tname :business-types
                  :data body
                  :where [:= :id (:id path-params)]})
  (res/response {:message "successfully updated business type"}))

(defn delete
  {:summary "delete business type with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "business type id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params] :as _request}]
  (db/delete! ds {:tname :business-types
                  :where [:= :id (:id path-params)]})
  (res/response {:message "successfully deleted business type"}))
