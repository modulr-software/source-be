(ns source.routes.bundle-feed
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [source.logger :as logger]))

(defn get
  {:summary "Get a single RSS feed by id from RSS feeds within the uuid-authorized bundle. 
   This endpoint will update click analytics for the returned RSS feed."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]
                :path [:map [:id {:title "id"
                                  :description "Feed ID"} :int]]}
   :responses (-> (api/success schemas/Feed)
                  (api/not-found))}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [feed (hon/find-one ds {:tname :feeds
                               :where [:= :id (:id path-params)]})]
    (try
      (analytics/insert-feed-click! ds feed bundle-id)
      (catch Exception e (logger/log-error (str "Failed to insert feed click for bundle feed: " (.getMessage e)))))
    (res/response feed)))
