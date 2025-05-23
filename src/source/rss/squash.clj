(ns source.rss.squash
  (:require [clojure.xml :as xml]
            [source.rss.util :as util]))

(defn squash
  "Needs a lot more support for different kinds of edge cases.
  Preferrably i would want this to be updated to use something
  like clojure walk. @kaidanTheron to investigate."
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
  (squash {:tag :title :attrs nil :content ["something"]}))

(comment

  (->>
   (xml/parse "https://en.search.wordpress.com/?f=feed&q=Dallas%20Mavericks")
   (squash)
   (:rss)
   (:channel)
   (:item)
   (first))

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
   (xml/parse "https://archive.org/services/collection-rss.php?query=description:Dallas%20Mavericks")))
