(ns source.routes.analytics.creator.top
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]
            [clojure.set :as set]
            [source.services.interface :as services]))

(defn record-names [ds top-field ids]
  (if (= top-field :post-id)
    (mapv (fn [{:keys [id title]}]
            {:id id
             :name title})
          (services/incoming-posts ds {:where [:in :id ids]}))
    (mapv (fn [{:keys [id name]}]
            {:id id
             :name name})
          (services/bundles ds {:where [:in :id ids]}))))

(defn get
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
