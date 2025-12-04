(ns source.routes.analytics.creator.deltas
  (:require [clojure.walk :as w]
            [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "Returns the percentage of growth in impressions, clicks and views per week, over the given time period. Optionally filtered by feed."
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]
                        [:feed {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:week :string]
                            [:impressions :float]
                            [:clicks :float]
                            [:views :float]]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [mindate maxdate feed]} (w/keywordize-keys query-params)]
    (let [results (analytics/weekly-growth-averages ds mindate maxdate {:creator-id (:id user)
                                                                        :feed-id feed})
          indexed-results (mapv (fn [{:keys [impressions clicks views]} i]
                                  {:week (str "week " i)
                                   :impressions impressions
                                   :clicks clicks
                                   :views views}) results (range 1 (inc (count results))))]
      (res/response indexed-results))))
