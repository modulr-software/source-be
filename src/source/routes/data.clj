(ns source.routes.data
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn post [{:keys [store body] :as _request}]
  (let [{:keys [_schema-id _url] :as opts} body]
    (-> (services/extract-data store opts)
        (res/response))))

(comment
  (require '[source.rss.youtube :as yt]
           '[source.datastore.interface :as store])

  (def url (->> "https://www.youtube.com/@ThePrimeTimeagen"
                (yt/find-channel-id)
                (str "https://www.youtube.com/feeds/videos.xml?channel_id=")))

  (post {:store (store/ds :datahike)
         :body {:schema-id 100
                :url url}})
  ())
