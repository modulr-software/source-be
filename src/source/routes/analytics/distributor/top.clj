(ns source.routes.analytics.distributor.top
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]
            [clojure.walk :as w]
            [clojure.set :as set]))

(defn get
  {:summary "Get the top n records with the highest number of impressions, clicks and views, in terms of the given top field. Optionally filtered by content type."
   :parameters {:query [:map
                        [:n :int]
                        [:mindate :string]
                        [:maxdate :string]
                        [:top [:enum "post" "feed"]]
                        [:contenttype {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:top :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [n mindate maxdate top contenttype]} (w/keywordize-keys query-params)
        top-field (if (= top "post") :post-id :feed-id)
        results (analytics/top-statistics-query ds mindate maxdate n top-field {:distributor-id (:id user)
                                                                                :content-type-id contenttype})]
    (res/response (mapv (fn [result]
                          (set/rename-keys result {top-field :top})) results))))
