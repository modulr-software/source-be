(ns source.db.bundle.analytics
  (:require [source.db.master.bundles :as bundles]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.password :as pw]
            [source.db.util :as db.util]))

(declare create-analytics-table)
(declare drop-analytics-table)
(declare select-all-analytics)
(declare insert-event)
(declare insert-events!)
(hugsql/def-db-fns "source/db/bundle/sql/analytics.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (def ds (db.util/conn "bundle-1"))
  (create-analytics-table ds)
  (insert-event ds {:cols ["post_id" "event_type" "timestamp"]
                    :vals [1 "impression" (.toString (new java.util.Date))]})
  (select-all-analytics ds)
  (drop-analytics-table ds)
  (insert-events! ds {:events [[1 "impression" (.toString (java.util.Date))]]}))
