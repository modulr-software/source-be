(ns source.services.interface
  (:require  [source.services.users :as users]
             [source.db.interface :as db]
             [source.services.auth :as auth]
             [source.services.xml-schemas :as xml]
             [source.services.bundles :as bundles]
             [source.services.bundle-categories :as bundle-categories]
             [source.services.bundle-content-types :as bundle-content-types]
             [source.services.post-heuristics :as post-heuristics]
             [source.services.providers :as providers]
             [source.services.feeds :as feeds]
             [source.services.incoming-posts :as incoming-posts]
             [source.services.outgoing-posts :as outgoing-posts]
             [source.services.cadences :as cadences]
             [source.services.baselines :as baselines]
             [source.services.content-types :as content-types]
             [source.services.categories :as categories]
             [source.services.feed-categories :as feed-categories]
             [source.services.jobs :as jobs]
             [source.services.businesses :as businesses]
             [source.services.user-sectors :as user-sectors]
             [source.services.filtered-feeds :as filtered-feeds]
             [source.services.filtered-posts :as filtered-posts]))

(defn users
  [& args]
  (apply users/users args))

(defn user [ds {:keys [_id] :as opts}]
  (users/user ds opts))

(defn insert-user! [ds {:keys [_values _ret] :as opts}]
  (users/insert-user! ds opts))

(defn update-user! [ds {:keys [_id _values _where] :as opts}]
  (users/update-user! ds opts))

(defn business
  [ds {:keys [_id _where] :as opts}]
  (businesses/business ds opts))

(defn businesses
  ([ds] (businesses ds {}))
  ([ds opts]
   (businesses/businesses ds opts)))

(defn insert-business! [ds {:keys [_values _ret] :as opts}]
  (businesses/insert-business! ds opts))

(defn update-business! [ds {:keys [_id _values _where] :as opts}]
  (businesses/update-business! ds opts))

(defn login [ds {:keys [_email] :as opts}]
  (auth/login ds opts))

(defn register [ds user]
  (auth/register ds user))

(defn insert-selection-schema! [store db {:keys [_schema _record] :as opts}]
  (xml/insert-selection-schema! store db opts))

(defn selection-schema [ds {:keys [_id] :as opts}]
  (xml/selection-schema ds opts))

(defn insert-bundle! [ds {:keys [_values _ret] :as opts}]
  (bundles/insert-bundle! ds opts))

(defn update-bundle! [ds {:keys [_id _data _where] :as opts}]
  (bundles/update-bundle! ds opts))

(defn bundles
  ([ds] (bundles ds {}))
  ([ds {:keys [_where] :as opts}]
   (bundles/bundles ds opts)))

(defn bundle [ds {:keys [_id _where] :as opts}]
  (bundles/bundle ds opts))

(defn bundle-categories
  ([ds] (bundle-categories ds {}))
  ([ds {:keys [_where] :as opts}]
   (bundle-categories/bundle-categories ds opts)))

(defn insert-bundle-category! [ds {:keys [_data _ret] :as opts}]
  (bundle-categories/insert-bundle-category! ds opts))

(defn delete-bundle-category! [ds {:keys [_id _where] :as opts}]
  (bundle-categories/delete-bundle-category! ds opts))

(defn category-id-by-bundle [ds {:keys [_bundle-id _where] :as opts}]
  (bundle-categories/category-id ds opts))

(defn bundle-content-types
  ([ds] (bundle-content-types ds {}))
  ([ds {:keys [_where] :as opts}]
   (bundle-content-types/bundle-content-types ds opts)))

(defn insert-bundle-content-types! [ds {:keys [_data _ret] :as opts}]
  (bundle-content-types/insert-bundle-content-types! ds opts))

(defn delete-bundle-content-types! [ds {:keys [_id _where] :as opts}]
  (bundle-content-types/delete-bundle-content-types! ds opts))

(defn content-types-by-bundle [ds {:keys [_bundle-id _where] :as opts}]
  (bundle-content-types/content-types-by-bundle ds opts))

(defn content-type-id [ds {:keys [_bundle-id _where] :as opts}]
  (bundle-content-types/content-type-id ds opts))

(defn insert-post-heuristics! [ds {:keys [_data] :as opts}]
  (post-heuristics/insert-post-heuristics! ds opts))

(defn update-post-heuristics! [ds {:keys [_id _data _where] :as opts}]
  (post-heuristics/update-post-heuristics! ds opts))

(defn post-heuristics
  ([ds] (post-heuristics ds {}))
  ([ds {:keys [_where] :as opts}]
   (post-heuristics/post-heuristics ds opts)))

(defn upsert-post-heuristics! [ds {:keys [_data] :as opts}]
  (post-heuristics/upsert-post-heuristics! ds opts))

(defn post-heuristic [ds {:keys [_id _where] :as opts}]
  (post-heuristics/post-heuristic ds opts))

(defn top-posts-by-heuristic [ds {:keys [_select _limit _heuristic] :as opts}]
  (post-heuristics/top-posts-by-heuristic ds opts))

(defn outgoing-posts
  ([ds] (outgoing-posts ds {}))
  ([ds {:keys [_where] :as opts}]
   (outgoing-posts/outgoing-posts ds opts)))

(defn outgoing-post [ds {:keys [_id _where] :as opts}]
  (outgoing-posts/outgoing-post ds opts))

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

(defn providers [ds]
  (providers/providers ds))

(defn provider [ds provider-id]
  (providers/provider ds provider-id))

(defn delete-provider! [ds provider-id]
  (providers/delete-provider! ds provider-id))

(defn insert-provider! [ds {:keys [_values _ret] :as opts}]
  (providers/insert-provider! ds opts))

(defn content-types [ds]
  (content-types/content-types ds))

(defn content-type [ds id]
  (content-types/content-type ds id))

(defn insert-content-type! [ds {:keys [_values _ret] :as opts}]
  (content-types/insert-content-type! ds opts))

(defn insert-feed! [ds {:keys [_data] :as opts}]
  (feeds/insert-feed! ds opts))

(defn update-feed! [ds {:keys [_id _data _where] :as opts}]
  (feeds/update-feed! ds opts))

(defn feeds
  ([ds]
   (feeds/feeds ds))
  ([ds {:keys [_where] :as opts}]
   (feeds/feeds ds opts)))

(defn feed [ds {:keys [_id] :as opts}]
  (feeds/feed ds opts))

(defn delete-feed! [ds {:keys [_id _where] :as opts}]
  (feeds/delete-feed! ds opts))

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

(defn delete-incoming-post! [ds {:keys [_id _where] :as opts}]
  (incoming-posts/delete-incoming-post! ds opts))

(defn insert-cadence! [ds {:keys [_values _ret] :as opts}]
  (cadences/insert-cadence! ds opts))

(defn cadences [ds]
  (cadences/cadences ds))

(defn cadence [ds id]
  (cadences/cadence ds id))

(defn insert-baseline! [ds {:keys [_values _ret] :as opts}]
  (baselines/insert-baseline! ds opts))

(defn baselines [ds]
  (baselines/baselines ds))

(defn baseline [ds id]
  (baselines/baseline ds id))

(defn insert-category! [ds {:keys [_data _ret] :as opts}]
  (categories/insert-category! ds opts))

(defn update-category! [ds {:keys [_id _data _where] :as opts}]
  (categories/update-category! ds opts))

(defn categories
  ([ds] (categories ds {}))
  ([ds {:keys [_where] :as opts}]
   (categories/categories ds opts)))

(defn category [ds {:keys [_id _where] :as opts}]
  (categories/category ds opts))

(defn feed-categories
  ([ds] (feed-categories ds {}))
  ([ds {:keys [_where] :as opts}]
   (feed-categories/feed-categories ds opts)))

(defn insert-feed-category! [ds {:keys [_data _ret] :as opts}]
  (feed-categories/insert-feed-category! ds opts))

(defn upsert-feed-categories! [ds {:keys [_data] :as opts}]
  (feed-categories/upsert-feed-categories! ds opts))

(defn delete-feed-category! [ds {:keys [_id _where] :as opts}]
  (feed-categories/delete-feed-category! ds opts))

(defn categories-by-feed [ds {:keys [_feed-id _where] :as opts}]
  (feed-categories/categories-by-feed ds opts))

(defn category-id-by-feed [ds {:keys [_feed-id _where] :as opts}]
  (feed-categories/category-id ds opts))

(defn user-sectors
  ([ds] (user-sectors ds {}))
  ([ds {:keys [_where] :as opts}]
   (user-sectors/user-sectors ds opts)))

(defn insert-user-sector! [ds {:keys [_data _ret] :as opts}]
  (user-sectors/insert-user-sector! ds opts))

(defn delete-user-sector! [ds {:keys [_id _where] :as opts}]
  (user-sectors/delete-user-sector! ds opts))

(defn sectors-by-user [ds {:keys [_user-id _where] :as opts}]
  (user-sectors/sectors-by-user ds opts))

(defn sector-id [ds {:keys [_user-id _where] :as opts}]
  (user-sectors/sector-id ds opts))

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

(defn insert-filtered-feeds! [ds {:keys [_data _ret] :as opts}]
  (filtered-feeds/insert-filtered-feeds! ds opts))

(defn update-filtered-feeds! [ds {:keys [_id _data _where] :as opts}]
  (filtered-feeds/update-filtered-feeds! ds opts))

(defn filtered-feeds
  ([ds] (filtered-feeds ds {}))
  ([ds {:keys [_where] :as opts}]
   (filtered-feeds/filtered-feeds ds opts)))

(defn delete-filtered-feed! [ds {:keys [_id _where] :as opts}]
  (filtered-feeds/delete-filtered-feed! ds opts))

(defn insert-filtered-posts! [ds {:keys [_data _ret] :as opts}]
  (filtered-posts/insert-filtered-posts! ds opts))

(defn update-filtered-posts! [ds {:keys [_id _data _where] :as opts}]
  (filtered-posts/update-filtered-posts! ds opts))

(defn filtered-posts
  ([ds] (filtered-posts ds {}))
  ([ds {:keys [_where] :as opts}]
   (filtered-posts/filtered-posts ds opts)))

(defn delete-filtered-post! [ds {:keys [_id _where] :as opts}]
  (filtered-posts/delete-filtered-post! ds opts))

(comment
  (users (db/ds :master))
  (user (db/ds :master) {:id 2})
  (login (db/ds :master) {:email "merveillevaneck@gmail.com"})
  ())
