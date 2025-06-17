(ns source.db.master.users-sectors
  (:require [hugsql.adapter.next-jdbc :as next-adapter]
            [hugsql.core :as hugsql]))

(declare create-table!)
(declare drop-table!)
(declare select-all)
(declare insert-feeds-sectors!)
(declare select-by-user-id)
(hugsql/def-db-fns "source/db/master/sql/users_sectors.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

