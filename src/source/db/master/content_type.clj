(ns source.db.master.content-type
  (:require [source.db.util :as db.util]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-content-types-table)
(declare drop-content-types-table)
(declare content-types)
(declare content-type)
(declare insert-content-type)

(hugsql/def-db-fns "source/db/master/sql/content_type.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (def ds (db.util/conn "master"))
  (create-content-types-table ds)
  (drop-content-types-table ds)
  (insert-content-type ds {:name "blog"})
  (content-types ds)
  (content-type ds {:id 1})
  )
