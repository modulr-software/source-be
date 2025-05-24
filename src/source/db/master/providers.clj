(ns source.db.master.providers
  (:require [source.db.util :as db.util]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-providers-table)
(declare drop-providers-table)
(declare insert-provider)
(declare providers)
(declare provider)

(hugsql/def-db-fns "source/db/master/sql/providers.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (def ds (db.util/conn "master"))
  (create-providers-table ds)
  (drop-providers-table ds)
  (providers ds)
  (insert-provider ds {:name "google"
                       :domain "google.com"
                       :content-type-id 1})
  (provider ds {:id 1})
  )
