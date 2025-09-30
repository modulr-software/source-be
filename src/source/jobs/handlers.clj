(ns source.jobs.handlers
  (:require [source.services.interface :as services]
            [source.util :as util]
            [source.services.incoming-posts :as incoming-posts]
            [source.db.util :as db.util]
            [clojure.set :as set]))

(defmulti handler
  (fn [opts]
    (keyword (:handler opts))))

(defmethod handler :default [opts]
  (throw (IllegalArgumentException.
          (str "Handler with name " (:handler opts) " does not exist."))))

(defmethod handler :test [_]
  (fn [{:keys [args]}]
    (println "hello" (get args :name) args)))

(defmethod handler :update-feed-posts [_]
  (fn [{:keys [args ds store]}]
    (try
      (let [{:keys [feed-id creator-id content-type-id provider-id url]} args
            selection-schemas (->> [:= :provider-id provider-id]
                                   (assoc {} :where)
                                   (services/selection-schemas ds))
            latest-ss (->> selection-schemas
                           (reduce (fn [acc {:keys [id]}]
                                     (conj acc id)) [])
                           (apply max -1))
            extracted (services/extract-data store {:schema-id latest-ss
                                                    :url url})
            extracted-posts (get-in extracted [:feed :posts])
            extended-posts (mapv (fn [post]
                                   (merge post
                                          {:feed-id feed-id
                                           :creator-id creator-id
                                           :content-type-id content-type-id})) extracted-posts)
            existing-posts (services/incoming-posts ds {:where [:= :creator-id creator-id]})]
        (services/update-feed! ds {:id feed-id
                                   :data {:title (get-in extracted [:feed :title])
                                          :updated-at (util/get-utc-timestamp-string)}})
        (run!
         (fn [post]
           (if (some #(= (:post-id post) (:post-id %)) existing-posts)
             (services/update-incoming-post! ds {:where [:= :post-id (:post-id post)]
                                                 :data post})
             (services/insert-incoming-post! ds {:data post})))
         extended-posts))
      (catch Exception _ :fail))))

; run long heuristics and pull the highest scoring incoming posts into the bundle's outgoing posts
(defmethod handler :update-bundle [_]
  (fn [{:keys [args ds]}]
    (let [{:keys [bundle-id categories]} args
          ds-bundle (db.util/conn :bundle bundle-id)
          incoming-posts (services/incoming-posts-with-feeds ds {:where [:= :feeds.state "live"]})
          posts-categories (incoming-posts/categories-by-posts ds {:where [:= :state "live"]})]
      (run!
       (fn [post]
          ; calculate score for post
          ; determine number of categories matched
              ; get vector of category ids in the given post, e.g. [1 3]
         (let [post-categories-vec (->> posts-categories
                                        (mapv (fn [{:keys [post-id id]}]
                                                (when (= post-id (:id post)) id)))
                                        (filterv identity))
               ; get vector of category ids in categories to match, e.g. [1 2 3 4]
               match-categories-vec (reduce (fn [acc {:keys [id]}]
                                              (conj acc id)) [] categories)
               ; get number of matches between the 2 vectors, e.g. #{1 3} intersect #{1 2 3 4} -> (count #{1 3}) -> 2
               matches (count (set/intersection (set post-categories-vec)
                                                (set match-categories-vec)))]
            ; use matches as a score and upsert long-heuristic for this post
           (services/upsert-post-heuristics! ds-bundle {:data [{:post-id (:id post)
                                                                :long-heuristic matches}]})))
       incoming-posts)

      ; pull highest scored posts by long heuristics into outgoing posts
            ; top 100 post-heuristics records ordered by long heuristic in descending order
      (let [top-by-long-heuristics (services/top-posts-by-heuristic ds-bundle
                                                                    {:heuristic :long-heuristic
                                                                     :limit 100})
            ; convert into a vector of id numbers
            ids (mapv :post-id top-by-long-heuristics)

            ; get all incoming posts with the above id numbers
            posts-in (services/incoming-posts ds {:where [:in :id ids]})
            ; remove redacted posts
            outgoing-posts (reduce (fn [acc {:keys [redacted] :as post}]
                                     (if (:= redacted 0)
                                       (conj acc (dissoc post :redacted))
                                       acc))
                                   [] posts-in)]
        (when (seq posts-in)
          (services/upsert-outgoing-posts! ds-bundle {:data outgoing-posts}))))))
