(ns source.db.bundle.event-category
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-table!)
(declare drop-table!)
(declare select-all)
(declare insert-event-category!)
(hugsql/def-db-fns "source/db/bundle/sql/event_category.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
