(ns source.routes.analytics.creator.top-average
  (:require [source.services.analytics.interface :as analytics]
            [ring.util.response :as res]
            [source.util :as utils]))

(defn get
  {:summary "Get the average engagement (clicks and views) for a creator, optionally filtered by content type.
   Date must be in the format yyyy-MM-dd"
   :parameters {:query [:map
                        [:mindate :string]
                        [:maxdate :string]
                        [:contenttype {:optional true} [:maybe :int]]]}
   :responses {200 {:body [:map [:average :float]]}}}

  [{:keys [ds user query-params] :as _request}]
  (let [{:keys [data success error]} (utils/validate get query-params :query)
        {:keys [mindate maxdate contenttype]} data]
    (if success
      (let [result (analytics/average-engagement ds mindate maxdate {:creator-id (:id user)
                                                                     :content-type-id contenttype})]
        (res/response {:average result}))

      (-> (res/response error)
          (res/status 400)))))
