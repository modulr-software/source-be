(ns source.db.master.baselines
  (:require [source.db.master.connection :refer [ds] :as c]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-baselines-table)
(declare drop-baselines-table)
(declare insert-baseline)
(declare baselines)
(declare baseline)

(hugsql/def-db-fns "source/db/master/sql/baselines.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (create-baselines-table ds)
  (drop-baselines-table ds)
  (baselines ds)
  (insert-baseline ds {:label "0-1000"
                    :min 0
                    :max 1000})
  )