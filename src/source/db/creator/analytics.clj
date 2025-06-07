(ns source.db.creator.analytics
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-table!)
(declare drop-table!)
(declare select-all)
(declare insert-event!)
(hugsql/def-db-fns "source/db/bundle/sql/analytics.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
