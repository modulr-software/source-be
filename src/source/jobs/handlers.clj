(ns source.jobs.handlers
  (:require [source.services.interface :as services]
            [source.workers.xml-schemas :as xml]
            [source.workers.users :as users]
            [source.util :as util]
            [source.services.incoming-posts :as incoming-posts]
            [source.db.util :as db.util]
            [clojure.set :as set]
            [clojure.string :as string]
            [source.db.honey :as hon]
            [source.logger :as logger]))

(defmulti handler
  (fn [opts]
    (keyword (:handler opts))))

(defmethod handler :default [opts]
  (throw (IllegalArgumentException.
          (str "Handler with name " (:handler opts) " does not exist."))))

(defmethod handler :test [_]
  (fn [{:keys [args]}]
    (println "hello" (get args :name) args)))

(defn update-feed-posts-job-id
  "returns the job id of an update-feed-posts job with the given email and feed-id"
  [email feed-id]
  (str email "-" feed-id))

(defmethod handler :update-feed-posts [_]
  (fn [{:keys [args ds]}]
    (try
      (when (users/removed? ds (:creator-id args))
        (let [{:keys [feed-id creator-id content-type-id provider-id url]} args
              _ (logger/log (str "feed " feed-id " job started."))
              selection-schemas (->> [:= :provider-id provider-id]
                                     (assoc {} :where)
                                     (xml/selection-schemas ds))
              latest-ss (->> selection-schemas
                             (reduce (fn [acc {:keys [id]}]
                                       (conj acc id)) [])
                             (apply max -1))
              extracted (try
                          (xml/extract-data ds latest-ss url)
                          (catch Exception e
                            (throw (ex-info (str "Data extraction for feed job failed: feed-id " feed-id " creator-id " creator-id)
                                            {:panic? "Yes, if data extraction fails here it will likely fail for others."
                                             :possible-cause "Could possibly be an incorrect selection schema or output schema"
                                             :next-steps (str "Check selection-schema-id " latest-ss " and feed-id " feed-id ". Test extraction manually.")
                                             :raw-error (.getMessage e)}))))

              extracted-posts (get-in extracted [:feed :posts])
              extracted-display (get-in extracted [:feed :display-picture])
              extended-posts (mapv (fn [{:keys [posted-at thumbnail] :as post}]
                                     (merge post
                                            {:feed-id feed-id
                                             :creator-id creator-id
                                             :content-type-id content-type-id
                                             :posted-at (util/format-rss-date posted-at)
                                             :thumbnail (if (and thumbnail
                                                                 (seq thumbnail)
                                                                 (not (string/includes? thumbnail ".mp3")))
                                                          thumbnail
                                                          extracted-display)}))
                                   extracted-posts)
              existing-posts (hon/find ds {:tname :incoming-posts
                                           :where [:= :creator-id creator-id]})
              existing-feed (hon/find-one ds {:tname :feeds
                                              :where [:= :id feed-id]})]
          (hon/update! ds {:tname :feeds
                           :where [:= :id feed-id]
                           :data {:title (get-in extracted [:feed :title])
                                  :description (get-in extracted [:feed :description])
                                  :display-picture (if (and (:display-picture existing-feed)
                                                            (seq (:display-picture existing-feed)))
                                                     (:display-picture existing-feed)
                                                     extracted-display)
                                  :updated-at (util/get-utc-timestamp-string)}})
          (run!
           (fn [post]
             (if (some #(= (:post-id post) (:post-id %)) existing-posts)
               (hon/update! ds {:tname :incoming-posts
                                :where [:= :post-id (:post-id post)]
                                :data post})
               (hon/insert! ds {:tname :incoming-posts
                                :data post})))
           extended-posts)
          (logger/log (str "feed " feed-id " job finished."))))

      (catch Exception e (logger/log-error (str "feed job failed: " e)) :fail))))

(defn update-bundle-job-id
  "returns the job id of an update-bundle job with the given bundle id"
  [bundle-id]
  (str "bundle_" bundle-id))

(defn determine-post-score [post posts-categories categories]
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
    ; use matches as a score to upsert long-heuristic for this post
    {:post-id (:id post)
     :long-heuristic matches}))

; run long heuristics and pull the highest scoring incoming posts into the bundle's outgoing posts
(defmethod handler :update-bundle [_]
  (fn [{:keys [args ds]}]
    (let [{:keys [bundle-id categories]} args
          _ (logger/log (str "starting bundle " bundle-id " job."))
          incoming-posts (services/incoming-posts-with-feeds ds {:where [:= :feeds.state "live"]})
          posts-categories (incoming-posts/categories-by-posts ds {:where [:= :state "live"]})
          heuristics (mapv
                      #(determine-post-score % posts-categories categories)
                      incoming-posts)]
      ; use precalculated heuristics and insert this data to the database
      (try
        (services/upsert-post-heuristics! ds {:bundle-id bundle-id
                                              :data heuristics})
        (catch Exception e (logger/log-error (str "bundle " bundle-id " upserting post heuristics failed: " (.getMessage e)))))

      ; pull highest scored posts by long heuristics into outgoing posts
            ; top 1000 post-heuristics records ordered by long heuristic in descending order
      (let [top-by-long-heuristics (services/top-posts-by-heuristic ds
                                                                    {:heuristic :long-heuristic
                                                                     :limit 2000
                                                                     :bundle-id bundle-id})
            ; convert into a vector of id numbers
            ids (mapv :post-id top-by-long-heuristics)

            ; get all incoming posts with the above id numbers
            posts-in (hon/find ds {:tname :incoming-posts
                                   :where [:in :id ids]})

            creator-ids (mapv :creator-id posts-in)
            active-creator-ids (->> (hon/find ds {:tname :users
                                                  :where [:in :id creator-ids]})
                                    (filterv #(or (nil? (:removed %)) (= (:removed %) 0)))
                                    (mapv :id))

            ; remove redacted posts
            outgoing-posts (reduce (fn [acc {:keys [redacted creator-id] :as post}]
                                     (if (and (or (nil? redacted) (= redacted 0))
                                              (some #{creator-id} active-creator-ids))
                                       (conj acc (dissoc post :redacted))
                                       acc))
                                   [] posts-in)]
        (when (seq posts-in)
          (hon/delete! ds (db.util/tname :outgoing-posts bundle-id))
          (hon/insert! ds (-> (db.util/tname :outgoing-posts bundle-id)
                              (assoc :data outgoing-posts)))
          (when (< (count outgoing-posts) 10)
            (throw (ex-info (str
                             "bundle job for bundle-id "
                             bundle-id
                             " pulled "
                             (count outgoing-posts)
                             ". Active creator id count: "
                             (count active-creator-ids)
                             ". Incoming posts pulled: "
                             (count posts-in))
                            {:panic? "Yes, the embed for this bundle will now be useless"
                             :possible-cause "If no posts made it into the bundle, it's possible post heuristics failed or there's no incoming posts"
                             :next-steps "Check for errors thrown in this job, ensure all tables for this bundle exist"}))))

        (logger/log (str "bundle " bundle-id " job done."))))))

(defn user-deletion-job-id
  "returns the job id of a user deletion job with the given user id"
  [user-type user-id]
  (str "delete_" user-type "_" user-id))

(defmethod handler :delete-user [_]
  (fn [{:keys [args ds]}]
    (try
      (let [{:keys [user-type user-id]} args]
        (users/hard-delete-user! ds (keyword user-type) user-id))
      (catch Exception e (logger/log-error (str "Failed to delete user-id " (:user-id args) ":" e)) :fail))))
