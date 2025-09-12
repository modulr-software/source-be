(ns source.routes.feed-categories
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all categories belonging to the feed with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:feed-id :int]
                            [:category-id :int]]]}}}
  [{:keys [ds path-params] :as _request}]
  (->> (services/categories-by-feed ds {:feed-id (:id path-params)})
       (res/response)))

(defn post
  {:summary "update categories belonging to the feed with the given id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]
                :body [:vector
                       [:map
                        [:id :int]
                        [:name :string]]]}
   :responses {200 {:body [:map [:message :string]]}}}
  [{:keys [ds path-params body] :as _request}]
  (let [update-data (reduce (fn [acc {:keys [id]}]
                              (conj acc {:feed-id (:id path-params)
                                         :category-id id})) [] body)]
    (services/delete-feed-category! ds {:where [:= :feed-id (:id path-params)]})
    (services/insert-feed-category! ds {:data update-data})
    (res/response {:message "successfully updated feed categories"})))
