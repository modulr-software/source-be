(ns source.db.master.categories
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-table)
(declare drop-table)
(declare select-all)
(declare select-by-id)
(declare insert)
(hugsql/def-db-fns "source/db/master/sql/categories.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
