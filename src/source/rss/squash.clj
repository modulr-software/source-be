(ns source.rss.squash
  (:require [source.rss.util :as util]))

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
