(ns source.rss.youtube
  (:require [clojure.string :as s]
            [source.rss.squash :as squash]
            [clojure.xml :as xml]))

(defn find-channel-id [url]
  (let [page (slurp url)]
    (->
     (re-find #"\"externalId\":\"(UC[-_A-Za-z0-9]{22})\"" page)
     (second)
     (s/split #"/")
     (last))))

(defn scrape-yt-channel [url]
  (->>
   (find-channel-id url)
   (str "https://www.youtube.com/feeds/videos.xml?channel_id=")
   (xml/parse)
   (squash/squash)))

(comment
  (find-channel-id "https://www.youtube.com/@CodingWithLewis")
  )