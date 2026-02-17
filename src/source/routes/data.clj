(ns source.routes.data
  (:require [source.workers.xml-schemas :as xml]
            [ring.util.response :as res]))

(defn post [{:keys [ds body] :as _request}]
  (let [{:keys [schema-id url]} body]
    (-> (xml/extract-data ds schema-id url)
        (res/response))))

(comment
  (require '[source.rss.youtube :as yt])

  (def url (->> "https://www.youtube.com/@ThePrimeTimeagen"
                (yt/find-channel-id)
                (str "https://www.youtube.com/feeds/videos.xml?channel_id=")))
  ())
