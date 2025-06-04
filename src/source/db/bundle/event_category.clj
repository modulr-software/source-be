(ns source.db.bundle.event-category
  (:require [source.db.master.bundles :as bundles]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.password :as pw]
            [source.db.util :as db.util]))

(declare create-event-category-join-table)
(declare drop-event-category-table)
(declare select-all-event-category)
(declare insert-event-category)
(hugsql/def-db-fns "source/db/bundle/sql/event_category.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (def ds (db.util/conn "bundle-1"))
  (create-event-category-join-table ds)
  (drop-event-category-table ds {:cols ["post_id" "event_type" "timestamp"]
                                 :vals [1 "impression" (.toString (new java.util.Date))]} ds)
  (select-all-event-category ds)
  (insert-event-category ds))
