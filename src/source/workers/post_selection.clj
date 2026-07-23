(ns source.workers.post-selection
  (:require [source.db.honey :as hon]
            [source.db.util :as db.util]
            [clojure.set :as set]
            [honey.sql.helpers :as hsql]))

(def ^:private selection-threshold 2000)

(defn bundle-content-type-ids
  "returns vec of content type ids associated with the given bundle. If there are
   no content type ids in bundle-content-types, returns all content type ids from
   the content-types table."
  [ds bundle-id]
  (let [ids (->> (hon/find ds {:tname :bundle-content-types
                               :where [:= :bundle-id bundle-id]})
                 (mapv :content-type-id))]
    (if (seq ids)
      ids
      (->> (hon/find ds {:tname :content-types})
           (mapv :id)))))

(defn bundle-category-ids
  "returns a vec of category ids associated with the given bundle. If there are
   no category ids in bundle-categories, returns all category ids from the
   categories table."
  [ds bundle-id]
  (let [ids (->> (db.util/tname :bundle-categories bundle-id)
                 (merge {:where [:= :bundle-id bundle-id]})
                 (hon/find ds)
                 (mapv :category-id))]
    (if (seq ids)
      ids
      (->> (hon/find ds {:tname :categories})
           (mapv :id)))))

(defn feed-categories
  "returns a vec of feeds with their associated category ids attached"
  [ds]
  (->> (hon/find ds {:tname :feeds
                     :where [:= :state "live"]})
       (mapv #(assoc % :category-ids (->> {:tname :feed-categories
                                           :where [:= :feed-id (:id %)]}
                                          (hon/find ds)
                                          (mapv :category-id))))))

(defn feeds-per-content-type
  "Returns a vec of maps containing each content type id with their corresponding feed-ids"
  [ds]
  (->> (hon/find ds {:tname :feeds})
       (group-by :content-type-id)
       (mapv (fn [[content-type-id feeds]]
               {:content-type-id content-type-id
                :feed-ids (mapv :id feeds)}))))

(defn filter-content-type
  "returns a filtered version of the dataset containing only those with content-type-ids matching the provided content type ids"
  [content-type-ids dataset]
  (let [valid-ids (set content-type-ids)]
    (filterv #(contains? valid-ids (:content-type-id %)) dataset)))

(defn filter-category-ids
  "returns a filtered version of the dataset containing only those with category-ids matching at least one of the provided category ids"
  [category-ids dataset]
  (let [valid-ids (set category-ids)]
    (filterv #(some valid-ids (:category-ids %)) dataset)))

(defn count-shared-categories
  "returns single int as count of distinct categories appearing both on the feed and the bundle's category set"
  [feed-categories category-ids]
  (count (set/intersection
          (set (mapcat :category-ids feed-categories))
          (set category-ids))))

(defn feed-posts
  "returns a vec of posts from all the feeds or feed-ids provided. Accepts either
   a vec of feed maps (extracting :id from each) or a vec of integer ids."
  [ds feeds-or-ids]
  (let [ids (if (map? (first feeds-or-ids))
              (mapv :id feeds-or-ids)
              feeds-or-ids)]
    (if (seq ids)
      (hon/find ds {:tname :incoming-posts
                    :where [:in :feed-id ids]})
      [])))

(defn bundle-post-times-selected
  "returns vec of maps of all post-ids that have been in the bundle, with the corresponding number of times they have been selected"
  [ds bundle-id post-ids]
  (let [counts (when (seq post-ids)
                 (->> (hon/execute!
                       ds
                       (-> (hsql/select :post-id [[:count :*] :times-selected])
                           (hsql/from :events)
                           (hsql/where [:and
                                        [:in :post-id post-ids]
                                        [:= :bundle-id bundle-id]
                                        [:= :event "selected"]])
                           (hsql/group-by :post-id))
                       :ret :*)
                      (into {} (map (juxt :post-id :times-selected)))))]
    (mapv (fn [post-id]
            {:post-id post-id
             :times-selected (get counts post-id 0)})
          post-ids)))

(defn avg-bundle-post-times-selected
  "returns the average no. times a post has been selected for the bundle, taking the result of bundle-post-times-selected as input"
  [times-selected]
  (let [selected (filterv #(pos? (:times-selected %)) times-selected)]
    (if (seq selected)
      (float
       (/ (reduce + (map :times-selected selected))
          (count selected)))
      0)))

(defn relevant-feeds
  "returns a vec of feeds that meet the content type and category criteria given by the bundle"
  [ds bundle-id]
  (let [content-type-ids (bundle-content-type-ids ds bundle-id)
        category-ids (bundle-category-ids ds bundle-id)]
    (->> (feed-categories ds)
         (filter-content-type content-type-ids)
         (filter-category-ids category-ids))))

(defn primary-selection [ds bundle-id]
  (let [category-ids (bundle-category-ids ds bundle-id)
        total-available (->> (hon/find ds {:tname :feeds
                                           :where [:= :state "live"]})
                             (feed-posts ds)
                             (count))

        relevant-posts (->> (relevant-feeds ds bundle-id)
                            (feed-posts ds))

        category-matched-posts (when (and (>= total-available selection-threshold)
                                          (< (count relevant-posts) selection-threshold))
                                 (->> (feed-categories ds)
                                      (filter-category-ids category-ids)
                                      (feed-posts ds)))]))

(comment
  (def ds (db.util/conn))
  (def bundle-id 26)

  (bundle-content-type-ids (db.util/conn) 26)

  (bundle-category-ids (db.util/conn) 26)

  (feed-categories ds)

  (->> (feed-categories ds)
       (filter-content-type [1]))

  (filter-category-ids
   (bundle-category-ids ds bundle-id)
   (feed-categories ds))

  (count-shared-categories
   (feed-categories ds)
   (bundle-category-ids ds bundle-id))

  (feed-posts ds (mapv :id
                       (filter-content-type
                        (feed-categories ds)
                        [1])))

  (->> (feed-categories ds)
       (filter-content-type [1])
       (feed-posts ds)
       (mapv :id)
       (bundle-post-times-selected ds bundle-id)
       (avg-bundle-post-times-selected))

  (->> (relevant-feeds ds bundle-id)
       (feed-posts ds)
       (mapv :id)
       (bundle-post-times-selected ds bundle-id)
       (filterv #(< (:times-selected %) 6.7))
       (count))

  (feeds-per-content-type ds)

  ())
