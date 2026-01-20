(ns source.routes.analytics.distributor.top-average
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]))

(defn get
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
