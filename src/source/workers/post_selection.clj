(ns source.workers.post-selection
  (:require [source.db.honey :as hon]
            [source.db.util :as db.util]
            [clojure.set :as set]))

(defn bundle-content-type-ids
  "returns vec of content type ids associated with the given bundle"
  [ds bundle-id]
  (->> (hon/find ds {:tname :bundle-content-types
                     :where [:= :bundle-id bundle-id]})
       (mapv #(:content-type-id %))))

(defn bundle-category-ids
  "returns a vec of category ids associated with the given bundle"
  [ds bundle-id]
  (->> (db.util/tname :bundle-categories bundle-id)
       (merge {:where [:= :bundle-id bundle-id]})
       (hon/find ds)
       (mapv #(:category-id %))))

(defn feed-categories
  "returns a vec of feeds with their associated category ids attached"
  [ds]
  (->> (hon/find ds {:tname :feeds
                     :where [:= :state "live"]})
       (mapv #(assoc % :category-ids (->> {:tname :feed-categories
                                           :where [:= :feed-id (:id %)]}
                                          (hon/find ds)
                                          (mapv :id))))))

(defn filter-content-type
  "returns a filtered version of the dataset containing only those with content-type-ids matching the provided content type ids"
  [dataset content-type-ids]
  (let [valid-ids (set content-type-ids)]
    (filterv #(contains? valid-ids (:content-type-id %)) dataset)))

(defn filter-category-ids
  "returns a filtered version of the dataset containing only those with category-ids matching at least one of the provided category ids"
  [dataset category-ids]
  (let [valid-ids (set category-ids)]
    (filterv #(some valid-ids (:category-ids %)) dataset)))

(defn count-shared-categories
  "returns single int as count of distinct categories appearing both on the feed and the bundle's category set"
  [feed-categories category-ids]
  (count (set/intersection
          (set (mapcat :category-ids feed-categories))
          (set category-ids))))

(defn feed-posts
  "returns a vec of posts from all the feed ids provided"
  [ds feed-ids]
  (if (seq feed-ids)
    (hon/find ds {:tname :incoming-posts
                  :where [:in :feed-id feed-ids]})
    []))

#_(defn bundle-post-times-selected
    "returns map of all post-ids that have been in the bundle, with the corresponding number of times they have been selected"
    [ds post-ids bundle-id])

#_(defn avg-bundle-post-times-selected
    "returns the average no. times a post has been selected for the bundle, taking the result of bundle-post-times-selected as input"
    [times-selected])

(comment
  (def ds (db.util/conn))
  (def bundle-id 26)

  (bundle-content-type-ids (db.util/conn) 26)

  (bundle-category-ids (db.util/conn) 26)

  (feed-categories ds)

  (filter-content-type
   (feed-categories ds)
   [1])

  (filter-category-ids
   (feed-categories ds)
   [190 191 192 193 194 195 196 197 198 199 200])

  (count-shared-categories
   (feed-categories ds)
   (bundle-category-ids (db.util/conn) 26))

  (feed-posts ds (mapv :id
                       (filter-content-type
                        (feed-categories ds)
                        [1])))

  ())
