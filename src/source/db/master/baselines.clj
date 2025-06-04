(ns source.db.master.baselines
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-baselines-table)
(declare drop-baselines-table)
(declare insert-baseline)
(declare baselines)
(declare baseline)

(hugsql/def-db-fns "source/db/master/sql/baselines.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
