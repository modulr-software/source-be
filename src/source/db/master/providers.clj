(ns source.db.master.providers
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-providers-table)
(declare drop-providers-table)
(declare insert-provider)
(declare providers)
(declare provider)

(hugsql/def-db-fns "source/db/master/sql/providers.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
