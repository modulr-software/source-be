(ns source.routes.creator-analytics
  (:require [clojure.set :as set]
            [clojure.walk :as w]
            [java-time.api :as jt]
            [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.services.interface :as services]))

(defn general-statistics
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

(defn weekly-growth-deltas
  {:summary "Returns the percentage of growth in impressions, clicks and views per week, over the given time period. Optionally filtered by feed.
   Date must be in the format yyyy-MM-dd"
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
                                                  (catch Exception _
                                                    (-> (res/response {:message "Invalid date format. Date must be in the format yyyy-MM-dd."})
                                                        (res/status 400))))
        days-between (.until parsed-mindate parsed-maxdate java.time.temporal.ChronoUnit/DAYS)
        results (analytics/weekly-growth-averages ds mindate maxdate {:creator-id (:id user)
                                                                      :feed-id feed})]
    (if (>= days-between 14)
      (res/response results)
      (-> (res/response {:message "Date range too small. Date range must include at least 2 weeks."})
          (res/status 400)))))

(defn- record-names [ds top-field ids]
  (if (= top-field :post-id)
    (mapv (fn [{:keys [id title]}]
            {:id id
             :name title})
          (services/incoming-posts ds {:where [:in :id ids]}))
    (mapv (fn [{:keys [id name]}]
            {:id id
             :name name})
          (services/bundles ds {:where [:in :id ids]}))))

(defn top-statistics
  {:summary "Get the top n records with the highest number of impressions, clicks and views, in terms of the given top field. Optionally filtered by content type.
   Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:n :int]
                        [:mindate :string]
                        [:maxdate :string]
                        [:top [:enum "post" "bundle"]]
                        [:contenttype {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:top :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [n mindate maxdate top contenttype]} query-params
        top-field (if (= top "post") :post-id :bundle-id)
        results (->> {:creator-id (:id user)
                      :content-type-id contenttype}
                     (analytics/top-statistics-query ds mindate maxdate n top-field)
                     (mapv (fn [result]
                             (set/rename-keys result {top-field :top}))))
        ids (mapv :top results)
        names (record-names ds top-field ids)
        juxted (->> names
                    (mapv (juxt :id :name))
                    (into {}))
        named-results (mapv (fn [{:keys [top] :as r}]
                              (assoc r :top (clojure.core/get juxted top (str top))))
                            results)]
    (res/response named-results)))

(defn average-engagement
  {:summary "Get the average engagement (clicks and views) for a creator, optionally filtered by content type.
   Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]
                        [:contenttype {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:map [:average :float]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [mindate maxdate contenttype]} query-params
        result (analytics/average-engagement ds mindate maxdate {:creator-id (:id user)
                                                                 :content-type-id contenttype})]
    (res/response {:average result})))
