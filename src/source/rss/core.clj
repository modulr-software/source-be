(ns source.rss.core
  (:require [clojure.xml :as xml]
            [clojure.string :as s]
            [clojure.data.json :as json]
            [source.rss.util :as util]))


(defn find-channel-id [url]
  (let [page (slurp url)]
    (->
     (re-find #"\"externalId\":\"(UC[-_A-Za-z0-9]{22})\"" page)
     (second)
     (s/split #"/")
     (last))))

(comment
  (find-channel-id "https://www.youtube.com/@CodingWithLewis"))

(defn squash
  ([item] (squash {} item))
  ([acc {:keys [content tag attrs] :as item}]
   (cond
     (and (nil? acc) (nil? item)) nil
     (nil? acc) (squash item)
     (nil? item) acc
     (string? item) item
     (and (map? item)
          (nil? content)) (assoc acc tag
                                 (-> (merge (into {} item) attrs)
                                     (dissoc :attrs :tag :content)))
     :else (let [mitem (into {} item)
                 parsed-content (reduce squash mitem content)
                 new-item (dissoc mitem :tag :attrs :content)
                 new-acc (if (string? parsed-content)
                           (util/stack acc {tag parsed-content})
                           (util/stack acc {tag (merge new-item
                                                  (dissoc parsed-content :tag :attrs :content)
                                                  attrs)}))]
             (dissoc (into {} new-acc) :tag :content :attrs)))))

(comment
  (squash {:tag :link :attrs {:href "http://www.youtube.com"} :content nil})
  (squash {:tag :title :attrs nil :content ["something"]})
  )


(defn scrape-yt-channel [url]
  (->>
   (find-channel-id url)
   (str "https://www.youtube.com/feeds/videos.xml?channel_id=")
   (xml/parse)
   (squash)))


(comment

  (->>
   (xml/parse "https://en.search.wordpress.com/?f=feed&q=Dallas%20Mavericks")
   (squash)
   (:rss)
   (:channel)
   (:item)
   (first)
   )

  (->
   (slurp "https://en.search.wordpress.com/?f=feed&q=Dallas%20Mavericks")
   ;;  (clojure.data.json/read-str {:key-fn keyword})
   ;;  (re-find #"www\.youtube\.com/channel/([A-Za-z0-9]+)")
   )

  (xml/parse "https://www.bing.com/news/search?format=RSS&q=database")
  (->
   (xml/parse "https://www.bing.com/news/search?format=RSS&q=database")
   ;; (transform-feed)
   )
  (->
   (xml/parse "https://en.search.wordpress.com/?f=feed&q=Dallas%20Mavericks"))
  (xml/parse "https://archive.org/services/collection-rss.php?query=description:Dallas%20Mavericks")
  (->
   (xml/parse "https://archive.org/services/collection-rss.php?query=description:Dallas%20Mavericks"))
  )