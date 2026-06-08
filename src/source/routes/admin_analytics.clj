(ns source.routes.admin-analytics
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]
            [source.routes.openapi :as api]))

(defn general
  {:summary "Overall impressions, clicks and views over the given time period. Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]]}
   :responses {200 {:body [:vector
                           [:map
                            [:day :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}

  [{:keys [ds query-params] :as _req}]
  (let [{:keys [mindate maxdate]} query-params
        results (analytics/interval-statistics-query ds :daily mindate maxdate {})]
    (res/response results)))

(defn feeds
  {:summary "Overall impressions, clicks and views for each feed live on Source"
   :responses {200 {:body [:vector
                           [:map
                            [:feed-id :int]
                            [:title :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}
  [{:keys [ds] :as _req}]
  (let [results (analytics/feed-statistics ds)]
    (res/response results)))

(defn distributors
  {:summary "Overall impressions, clicks and views for each distributor on Source"
   :responses {200 {:body [:vector
                           [:map
                            [:distributor-id :int]
                            [:bundle-id :int]
                            [:name :int]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}
  [{:keys [ds] :as _req}]
  (let [results (analytics/distributor-statistics ds)]
    (res/response results)))

(defn categories
  {:summary "Overall impressions, clicks and views for each category in Source"
   :responses {200 {:body [:vector
                           [:map
                            [:category-id :int]
                            [:name :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}
  [{:keys [ds] :as _req}]
  (let [results (analytics/category-statistics ds)]
    (res/response results)))

(defn top
  {:summary "Paginated list of top performing posts by impressions, clicks and views"
   :parameters (api/params :query [:map
                                   [:start :int]
                                   [:limit :int]])
   :responses {200 {:body (api/paginated [:vector
                                          [:map
                                           [:post-id :int]
                                           [:feed-id :int]
                                           [:title :string]
                                           [:feed-title :string]
                                           [:impressions :int]
                                           [:clicks :int]
                                           [:views :int]]])}}}
  [{:keys [ds query-params] :as _req}]
  (let [{:keys [start limit]} query-params
        results (analytics/top-post-statistics ds {:start start
                                                   :limit limit})]
    (res/response results)))
