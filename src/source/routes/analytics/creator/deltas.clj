(ns source.routes.analytics.creator.deltas
  (:require [clojure.walk :as w]
            [ring.util.response :as res]
            [java-time.api :as jt]
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
                            [:views :float]]]}
               400 {:body [:map [:message :string]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [mindate maxdate feed]} (w/keywordize-keys query-params)
        {:keys [parsed-mindate parsed-maxdate]} (try
                                                  {:parsed-mindate (jt/local-date mindate)
                                                   :parsed-maxdate (jt/local-date maxdate)}
                                                  (catch Exception _ (-> (res/response {:message "Invalid date format. Date must be in the format YYYY-MM-DD."})
                                                                         (res/status 400))))
        days-between (.until parsed-mindate parsed-maxdate java.time.temporal.ChronoUnit/DAYS)
        results (analytics/weekly-growth-averages ds mindate maxdate {:creator-id (:id user)
                                                                      :feed-id feed})
        indexed-results (mapv (fn [{:keys [impressions clicks views]} i]
                                {:week (str "week " i)
                                 :impressions impressions
                                 :clicks clicks
                                 :views views}) results (range 1 (inc (count results))))]
    (if (>= days-between 14)
      (res/response indexed-results)
      (-> (res/response {:message "Date range too small. Date range must include at least 2 weeks."})
          (res/status 400)))))
