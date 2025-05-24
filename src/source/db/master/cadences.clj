(ns source.db.master.cadences
  (:require [source.db.util :as db.util]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-cadences-table)
(declare drop-cadences-table)
(declare insert-cadence)
(declare cadences)
(declare cadence)

(hugsql/def-db-fns "source/db/master/sql/cadences.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (def ds (db.util/conn "master"))
  (create-cadences-table ds)
  (drop-cadences-table ds)
  (insert-cadence ds {:label "daily" :days 1})
  (cadences ds)
  (cadence ds {:id 1})
  )
