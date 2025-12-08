(ns source.routes.analytics.creator.general
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]
            [clojure.walk :as w]))

(defn get
  {:summary "Gets the number of impressions, clicks and views per day for a creator over the given time period. Optionally filtered by feed.
   Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]
                        [:feed {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:day :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [mindate maxdate feed]} (w/keywordize-keys query-params)]
    (res/response (analytics/interval-statistics-query ds :daily mindate maxdate {:creator-id (:id user)
                                                                                  :feed-id feed}))))
