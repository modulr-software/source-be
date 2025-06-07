(ns source.db.bundle.analytics
  (:require [source.db.master.bundles :as bundles]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.password :as pw]
            [source.db.util :as db.util]))

(declare create-table!)
(declare drop-table!)
(declare select-all)
(declare insert-event!)
(hugsql/def-db-fns "source/db/bundle/sql/analytics.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
