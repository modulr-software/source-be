(ns source.routes.bundle-analytics
  (:require [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.services.interface :as services]
            [source.db.honey :as hon]))

(defn feed-click
  {:summary "Explicitly insert a click event for the feed with the given id for the purpose of analytics"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:id {:title "id"
                                  :description "post id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [feed (hon/find-one ds {:tname :feeds
                               :where [:= :id (:id path-params)]})]
    (analytics/insert-feed-click! ds feed bundle-id)
    (res/response {:message "Successfully inserted click event"})))

(defn feed-impressions
  {:summary "Explicitly insert impression events for the given list of feed ids for the purpose of analytics"
   :parameters {:query [:map [:uuid :string]]
                :body [:map
                       [:ids [:vector :int]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id body] :as _request}]
  (let [feeds (hon/find ds {:tname :feeds
                            :where [:in :id (:ids body)]})]
    (analytics/insert-feed-impressions! ds feeds bundle-id)
    (res/response {:message (str "Successfully inserted impression events for " (count feeds) " feeds")})))

(defn post-view
  {:summary "Explicitly insert a view event for the post with the given id for the purpose of analytics"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:id {:title "id"
                                  :description "post id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [post (services/incoming-post ds {:id (:id path-params)})]
    (analytics/insert-post-view! ds post bundle-id)
    (res/response {:message "Successfully inserted view event"})))

(defn post-click
  {:summary "Explicitly insert a click event for the post with the given id for the purpose of analytics"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:id {:title "id"
                                  :description "post id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [post (services/incoming-post ds {:id (:id path-params)})]
    (analytics/insert-post-click! ds post bundle-id)
    (res/response {:message "Successfully inserted click event"})))

(defn post-impressions
  {:summary "Explicitly insert impression events for the given list of post ids for the purpose of analytics"
   :parameters {:query [:map [:uuid :string]]
                :body [:map
                       [:ids [:vector :int]]]}
   :responses {200 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id body] :as _request}]
  (let [posts (hon/find ds {:tname :incoming-posts
                            :where [:in :id (:ids body)]})]
    (analytics/insert-post-impressions! ds posts bundle-id)
    (res/response {:message (str "Successfully inserted impression events for " (count posts) " posts")})))
