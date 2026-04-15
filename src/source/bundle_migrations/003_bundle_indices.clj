(ns source.bundle-migrations.003-bundle-indices
  (:require [source.db.bundle]
            [pg.core :as pg]
            [source.db.util :as db.util]
            [camel-snake-kebab.core :as csk]))

(defn run-up! [context]
  (let [{:keys [ds-master bundle-id]} context
        op (->> bundle-id
                (db.util/tname :outgoing-posts)
                (:tname)
                (csk/->snake_case_string))]

    (pg/with-connection [ds-master ds-master]
      (pg/execute ds-master (str "CREATE INDEX idx_" op "_feed_id ON " op " (feed_id);"))
      (pg/execute ds-master (str "CREATE INDEX idx_" op "_creator_id ON " op " (creator_id);"))
      (pg/execute ds-master (str "CREATE INDEX idx_" op "_content_type_id ON " op " (content_type_id);")))))

(defn run-down! [context]
  (let [{:keys [ds-master bundle-id]} context
        op (->> bundle-id
                (db.util/tname :outgoing-posts)
                (:tname)
                (csk/->snake_case_string))]

    (pg/with-connection [ds-master ds-master]
      (pg/execute ds-master (str "DROP INDEX IF EXISTS idx_" op "_content_type_id"))
      (pg/execute ds-master (str "DROP INDEX IF EXISTS idx_" op "_creator_id;"))
      (pg/execute ds-master (str "DROP INDEX IF EXISTS idx_" op "_feed_id;")))))
