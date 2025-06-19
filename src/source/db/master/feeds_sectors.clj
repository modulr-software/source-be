(ns source.db.master.feeds-sectors
  (:require [hugsql.adapter.next-jdbc :as next-adapter]
            [hugsql.core :as hugsql]))

(declare create-table!)
(declare drop-table!)
(declare select-all)
(declare insert-feeds-sectors!)
(declare select-by-feed-id)
(hugsql/def-db-fns "source/db/master/sql/feeds_sectors.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

