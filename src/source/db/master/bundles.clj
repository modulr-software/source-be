(ns source.db.master.bundles
  (:require [source.db.master.bundles :as bundles]
            [source.db.master.connection :refer [ds] :as c]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.password :as pw]))

(declare create-bundles-table)
(declare drop-bundles-table)
(declare insert-bundle)
(declare select-all-bundles)
(hugsql/def-db-fns "source/db/master/sql/bundles.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (create-bundles-table ds)
  (drop-bundles-table ds)
  (insert-bundle ds {:cols ["user_id" "hash"] :vals [1 (pw/hash-password "test")]})
  (select-all-bundles ds)
  )