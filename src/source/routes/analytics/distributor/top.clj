(ns source.routes.analytics.distributor.top
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]
            [clojure.set :as set]
            [source.util :as utils]
            [source.db.honey :as hon]))

(defn record-names [ds top-field ids]
  (if (= top-field :post-id)
    (hon/find ds {:tname :incoming-posts
                  :where [:in :id ids]})
    (hon/find ds {:tname :feeds
                  :where [:in :id ids]})))

(defn get
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
  (let [{:keys [data success error]} (utils/validate get query-params :query)
        {:keys [n mindate maxdate top contenttype]} data
        top-field (if (= top "post") :post-id :feed-id)]
    (if success
      (let [results (->> {:distributor-id (:id user)
                          :content-type-id contenttype}
                         (analytics/top-statistics-query ds mindate maxdate n top-field)
                         (mapv (fn [result]
                                 (set/rename-keys result {top-field :top}))))
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
        (res/response named-results))

      (-> (res/response error)
          (res/status 400)))))
