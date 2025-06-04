(ns source.db.master.cadences
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-cadences-table)
(declare drop-cadences-table)
(declare insert-cadence)
(declare cadences)
(declare cadence)

(hugsql/def-db-fns "source/db/master/sql/cadences.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
