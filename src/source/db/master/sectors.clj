(ns source.db.master.sectors
  (:require [source.db.util :as db.util]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-sectors-table)
(declare drop-sectors-table)
(declare insert-sector)
(declare sectors)
(declare sector)
(declare sectors)

(hugsql/def-db-fns "source/db/master/sql/sectors.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (def ds (db.util/conn "master"))
  (create-sectors-table ds)
  (drop-sectors-table ds)

  (insert-sector ds {:name "something"})
  (sectors ds)

  (sector ds {:id 1})
  )
