(ns source.db.master.users
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-users-table)
(declare drop-users-table)
(declare insert-into-users)
(declare insert-user)
(declare update-user!)
(declare find-users-by-name)
(declare users)
(declare user)
(declare user-by)

(hugsql/def-db-fns "source/db/master/sql/users.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
