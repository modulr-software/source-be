(ns source.db.master.feeds
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]))

(declare drop-table)
(declare select-all)
(declare insert)
(declare select-by-id)
(declare create-table)

(hugsql/def-db-fns "source/db/master/sql/feeds.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
