(ns source.db.creator.analytics
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-analytics-table)
(declare drop-analytics-table)
(declare select-all-analytics)
(declare insert-event)
(declare insert-events!)
(hugsql/def-db-fns "source/db/bundle/sql/analytics.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
