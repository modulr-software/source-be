(ns source.db.master.content-type
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-content-types-table)
(declare drop-content-types-table)
(declare content-types)
(declare content-type)
(declare insert-content-type)

(hugsql/def-db-fns "source/db/master/sql/content_type.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
