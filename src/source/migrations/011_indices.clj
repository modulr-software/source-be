(ns source.migrations.011-indices
  (:require [source.db.master]
            [pg.core :as pg]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (pg/with-connection [ds-master ds-master]
      (pg/execute ds-master "CREATE INDEX idx_incoming_posts_feed_id ON incoming_posts (feed_id);")
      (pg/execute ds-master "CREATE INDEX idx_incoming_posts_creator_id ON incoming_posts (creator_id);")
      (pg/execute ds-master "CREATE INDEX idx_incoming_posts_content_type_id ON incoming_posts (content_type_id);")

      (pg/execute ds-master "CREATE INDEX idx_feeds_user_id ON feeds (user_id);")
      (pg/execute ds-master "CREATE INDEX idx_feeds_content_type_id ON feeds (content_type_id);")

      (pg/execute ds-master "CREATE INDEX idx_feed_categories_feed_id ON feed_categories (feed_id);")
      (pg/execute ds-master "CREATE INDEX idx_feed_categories_category_id ON feed_categories (category_id);")

      (pg/execute ds-master "CREATE INDEX idx_bundles_user_id ON bundles (user_id);")
      (pg/execute ds-master "CREATE INDEX idx_bundles_content_type_id ON bundles (content_type_id);")

      (pg/execute ds-master "CREATE INDEX idx_filtered_feeds_bundle_id ON filtered_feeds (bundle_id);")
      (pg/execute ds-master "CREATE INDEX idx_filtered_feeds_feed_id ON filtered_feeds (feed_id);")

      (pg/execute ds-master "CREATE INDEX idx_filtered_posts_bundle_id ON filtered_posts (bundle_id);")
      (pg/execute ds-master "CREATE INDEX idx_filtered_posts_post_id ON filtered_posts (post_id);"))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (pg/with-connection [ds-master ds-master]
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_incoming_posts_feed_id;")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_incoming_posts_creator_id;")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_incoming_posts_content_type_id;")

      (pg/execute ds-master "DROP INDEX IF EXISTS idx_feeds_user_id;")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_feeds_content_type_id;")

      (pg/execute ds-master "DROP INDEX IF EXISTS idx_feed_categories_feed_id;")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_feed_categories_category_id;")

      (pg/execute ds-master "DROP INDEX IF EXISTS idx_bundles_user_id")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_bundles_content_type_id")

      (pg/execute ds-master "DROP INDEX IF EXISTS idx_filtered_feeds_bundle_id")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_filtered_feeds_feed_id")

      (pg/execute ds-master "DROP INDEX IF EXISTS idx_filtered_posts_bundle_id")
      (pg/execute ds-master "DROP INDEX IF EXISTS idx_filtered_posts_post_id"))))
