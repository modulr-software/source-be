(ns source.db.master.businesses
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-table!)
(declare drop-table!)
(declare insert-business!)

(hugsql/def-db-fns "source/db/master/sql/businesses.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
