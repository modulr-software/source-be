(ns source.services.interface
  (:require  [source.services.users :as users]
             [source.services.auth :as auth]
             [source.services.xml-schemas :as xml]
             [source.services.bundles :as bundles]
             [source.services.bundle-content-types :as bundle-content-types]
             [source.services.post-heuristics :as post-heuristics]
             [source.services.providers :as providers]
             [source.services.feeds :as feeds]
             [source.services.incoming-posts :as incoming-posts]
             [source.services.outgoing-posts :as outgoing-posts]
             [source.services.feed-categories :as feed-categories]
             [source.services.jobs :as jobs]))

(defn user [ds {:keys [_id] :as opts}]
  (users/user ds opts))

(defn register [ds user]
  (auth/register ds user))

(defn insert-selection-schema! [store db {:keys [_schema _record] :as opts}]
  (xml/insert-selection-schema! store db opts))

(defn selection-schema [ds {:keys [_id] :as opts}]
  (xml/selection-schema ds opts))

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

(defn insert-outgoing-post! [ds {:keys [_values _ret] :as opts}]
  (outgoing-posts/insert-outgoing-post! ds opts))

(defn delete-outgoing-post! [ds {:keys [_id _where] :as opts}]
  (outgoing-posts/delete-outgoing-post! ds opts))

(defn selection-schemas
  ([ds]
   (selection-schemas ds {}))
  ([ds opts]
   (xml/selection-schemas ds opts)))

(defn delete-selection-schemas-by-provider! [store db provider-id]
  (xml/delete-selection-schemas-by-provider! store db provider-id))

(defn ast [url]
  (xml/ast url))

(defn extract-data [store {:keys [schema-id url]}]
  (xml/extract-data store schema-id url))

(defn output-schemas [store]
  (xml/output-schemas store))

(defn output-schema [store output-schema-id]
  (xml/output-schema store output-schema-id))

(defn insert-output-schema! [store schema]
  (xml/insert-output-schema! store schema))

(defn provider [ds provider-id]
  (providers/provider ds provider-id))

(defn update-feed! [ds {:keys [_id _data _where] :as opts}]
  (feeds/update-feed! ds opts))

(defn feeds
  ([ds]
   (feeds/feeds ds))
  ([ds {:keys [_where] :as opts}]
   (feeds/feeds ds opts)))

(defn feed [ds {:keys [_id] :as opts}]
  (feeds/feed ds opts))

(defn insert-incoming-post! [ds {:keys [_data _ret] :as opts}]
  (incoming-posts/insert-incoming-post! ds opts))

(defn update-incoming-post! [ds {:keys [_id _data _where] :as opts}]
  (incoming-posts/update-incoming-post! ds opts))

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
