(ns source.routes.distributor-analytics
  (:require [clojure.set :as set]
            [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]))

(defn- record-names [ds top-field ids]
  (if (= top-field :post-id)
    (hon/find ds {:tname :incoming-posts
                  :where [:in :id ids]})
    (hon/find ds {:tname :feeds
                  :where [:in :id ids]})))

(defn general-statistics
  {:summary "Gets the number of impressions, clicks and views per day for a distributor over the given time period. Optionally filtered by bundle.
   Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]
                        [:bundle {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:day :string]
                            [:impressions :int]
                            [:clicks :int]
                            [:views :int]]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [mindate maxdate bundle]} query-params]
    (res/response (analytics/interval-statistics-query ds :daily mindate maxdate {:distributor-id (:id user)
                                                                                  :bundle-id bundle}))))

(defn top-statistics
  {:summary "Get the top n records with the highest number of impressions, clicks and views, in terms of the given top field. Optionally filtered by content type.
   Date must be in the format yyyy-MM-dd"
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
  (let [{:keys [n mindate maxdate top contenttype]} query-params
        top-field (if (= top "post") :post-id :feed-id)
        results (->> {:distributor-id (:id user)
                      :content-type-id contenttype}
                     (analytics/top-statistics-query ds mindate maxdate n top-field)
                     (mapv #(set/rename-keys % {top-field :top})))
        ids (mapv :top results)
        names (record-names ds top-field ids)
        juxted (->> names
                    (mapv (fn [{:keys [id title]}]
                            {:id id
                             :name title}))
                    (mapv (juxt :id :name))
                    (into {}))
        named-results (mapv (fn [{:keys [top] :as r}]
                              (assoc r :top (clojure.core/get juxted top (str top))))
                            results)]
    (res/response named-results)))

(defn average-engagement
  {:summary "Get the average engagement (clicks and views) for a distributor, optionally filtered by content type.
   Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]
                        [:contenttype {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:map [:average :float]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [mindate maxdate contenttype]} query-params
        result (analytics/average-engagement ds mindate maxdate {:distributor-id (:id user)
                                                                 :content-type-id contenttype})]
    (res/response {:average result})))
