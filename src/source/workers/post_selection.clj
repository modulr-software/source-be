(ns source.workers.post-selection
  (:require [source.db.honey :as hon]
            [source.db.util :as db.util]))

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

#_(defn feed-categories
    "returns a vec of feeds with their associated category ids attached"
    [ds])

#_(defn filter-content-type
    "returns a filtered version of the dataset containing only those with content-type-ids matching the provided content type ids"
    [dataset content-type-ids])

#_(defn count-shared-categories
    "returns single int as count of distinct categories appearing both on the posts inherited feed and the bundle's category set"
    [feed-categories category-ids])

#_(defn bundle-post-times-selected
    "returns map of all post-ids that have been in the bundle, with the corresponding number of times they have been selected"
    [ds post-ids bundle-id])

#_(defn avg-bundle-post-times-selected
    "returns the average no. times a post has been selected for the bundle, taking the result of bundle-post-times-selected as input"
    [times-selected])

(comment
  (def ds (db.util/conn))
  (def bundle-id 14)

  (bundle-content-type-ids (db.util/conn) 14)

  (bundle-category-ids (db.util/conn) 14)
  ())
