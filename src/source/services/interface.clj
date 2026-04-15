(ns source.services.interface
  (:require [source.services.auth :as auth]
            [source.services.bundles :as bundles]
            [source.services.bundle-content-types :as bundle-content-types]
            [source.services.post-heuristics :as post-heuristics]
            [source.services.incoming-posts :as incoming-posts]
            [source.services.feed-categories :as feed-categories]
            [source.services.jobs :as jobs]))

(defn register [ds user]
  (auth/register ds user))

(defn update-bundle! [ds {:keys [_id _data _where] :as opts}]
  (bundles/update-bundle! ds opts))

(defn bundles
  ([ds] (bundles ds {}))
  ([ds {:keys [_where] :as opts}]
   (bundles/bundles ds opts)))

(defn bundle [ds {:keys [_id _where] :as opts}]
  (bundles/bundle ds opts))

(defn insert-bundle-content-types! [ds {:keys [_data _ret] :as opts}]
  (bundle-content-types/insert-bundle-content-types! ds opts))

(defn content-types-by-bundle [ds {:keys [_bundle-id _where] :as opts}]
  (bundle-content-types/content-types-by-bundle ds opts))

(defn upsert-post-heuristics! [ds {:keys [_data] :as opts}]
  (post-heuristics/upsert-post-heuristics! ds opts))

(defn top-posts-by-heuristic [ds {:keys [_select _limit _heuristic] :as opts}]
  (post-heuristics/top-posts-by-heuristic ds opts))

(defn incoming-posts
  ([ds]
   (incoming-posts/incoming-posts ds))
  ([ds {:keys [_where] :as opts}]
   (incoming-posts/incoming-posts ds opts)))

(defn incoming-posts-with-feeds
  [ds {:keys [_where] :as opts}]
  (incoming-posts/incoming-posts-with-feeds ds opts))

(defn incoming-post [ds opts]
  (incoming-posts/incoming-post ds opts))

(defn categories-by-feed [ds {:keys [_feed-id _where] :as opts}]
  (feed-categories/categories-by-feed ds opts))

(defn insert-job! [ds {:keys [_data _ret] :as opts}]
  (jobs/insert-job! ds opts))

(defn update-job! [ds {:keys [_id _data _where] :as opts}]
  (jobs/update-job! ds opts))

(defn delete-job! [ds {:keys [_id _where] :as opts}]
  (jobs/delete-job! ds opts))

(defn jobs
  ([ds] (jobs ds {}))
  ([ds opts]
   (jobs/jobs ds opts)))

(defn job [ds {:keys [_id _where] :as opts}]
  (jobs/job ds opts))

(defn insert-job-metadata! [ds {:keys [_data _ret] :as opts}]
  (jobs/insert-job-metadata! ds opts))

(defn update-job-metadata! [ds {:keys [_id _data _where] :as opts}]
  (jobs/update-job-metadata! ds opts))

(defn delete-job-metadata! [ds {:keys [_id _where] :as opts}]
  (jobs/delete-job-metadata! ds opts))

(defn job-metadata [ds {:keys [_id _where] :as opts}]
  (jobs/job-metadata ds opts))
