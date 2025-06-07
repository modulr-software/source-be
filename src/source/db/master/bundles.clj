(ns source.db.master.bundles
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-bundles-table)
(declare drop-bundles-table)
(declare insert-bundle)
(declare select-all-bundles)
(hugsql/def-db-fns "source/db/master/sql/bundles.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
