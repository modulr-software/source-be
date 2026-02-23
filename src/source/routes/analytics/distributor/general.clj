(ns source.routes.analytics.distributor.general
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]))

(defn get
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
