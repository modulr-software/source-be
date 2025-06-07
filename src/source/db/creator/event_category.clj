(ns source.db.creator.event-category
  (:require [source.db.master.bundles :as bundles]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.password :as pw]
            [source.db.util :as db.util]))

(declare create-table!)
(declare drop-table!)
(declare select-all)
(declare insert-event-category!)
(hugsql/def-db-fns "source/db/creator/sql/event_category.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
