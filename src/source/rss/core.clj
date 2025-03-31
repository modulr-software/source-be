(ns source.rss.core
  (:require [clojure.xml :as xml]
            [clojure.string :as s]))

(defn find-channel-id [url]
  (let [page (slurp url)]
    (->
     (re-find #"www\.youtube\.com/channel/([A-Za-z0-9]+)" page)
     (second)
     (s/split #"/")
     (last))))

(comment
  (find-channel-id "https://www.youtube.com/@EmilHansius")
  )

(defn transform-feed 
  ([feed]
   (transform-feed feed {}))
  ([feed acc]
   (cond
     (map? feed)
     (->
     (assoc acc (:tag feed)
            (when (vector? (:content feed))
              (let [parsed-content
                    (mapv (fn [v] (transform-feed v)) (:content feed))]
                (if (= 1 (count parsed-content))
                  (first parsed-content)
                  (into (sorted-map) parsed-content)))
              ))
      (assoc :attributes (:attrs feed)))
     (string? feed)
     feed
     :else nil)))

(defn scrape-yt-channel [url]
  (->>
   (find-channel-id url)
   (str "https://www.youtube.com/feeds/videos.xml?channel_id=")
   (xml/parse)
   (transform-feed)
   ))

(comment
  (scrape-yt-channel "https://www.youtube.com/@EmilHansius")
   (xml/parse "https://www.bing.com/news/search?format=RSS&q=database")
  (->
   (xml/parse "https://www.bing.com/news/search?format=RSS&q=database")
   (transform-feed))
  (->
   (xml/parse "https://en.search.wordpress.com/?f=feed&q=Dallas%20Mavericks")
   (transform-feed))
   (xml/parse "https://archive.org/services/collection-rss.php?query=description:Dallas%20Mavericks")
  (->
   (xml/parse "https://archive.org/services/collection-rss.php?query=description:Dallas%20Mavericks")
   (transform-feed))
  )