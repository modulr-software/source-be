(ns source.migrations.001-init-master-db
  (:require [source.db.master.users :as users]
            [source.db.master.bundles :as bundles]
            [source.db.master.baselines :as baselines]
            [source.db.master.feeds :as feeds]
            [source.db.master.feeds-categories :as feeds-categories]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.categories :as categories]
            [source.db.master.content-type :as content-types]
            [source.db.util :as db.util]
            [source.db.master.core :as master]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (users/create-users-table ds-master)
    (bundles/create-bundles-table ds-master)
    (baselines/create-baselines-table ds-master)
    (feeds/create-table! ds-master)
    (feeds-categories/create-table! ds-master)
    (providers/create-providers-table ds-master)
    (sectors/create-sectors-table ds-master)
    (categories/create-table ds-master)
    (content-types/create-content-types-table ds-master)
    (master/seed-table {:table "baselines"
                        :cols ["label" "min" "max"]
                        :vals [["0-1000" 0 1000]
                               ["1000-10000" 1000 10000]
                               ["10000-100000" 10000 100000]
                               ["100000-1000000" 100000 1000000]]})
    (master/seed-table {:table "cadences"
                        :cols ["label" "days"]
                        :vals [["daily" 1]
                               ["weekly" 7]
                               ["biweekly" 14]
                               ["monthly" 30]]})
    (master/seed-table {:table "content_types"
                        :cols ["name"]
                        :vals [["video"]
                               ["podcast"]
                               ["blog"]]})
    (master/seed-table {:table "providers"
                        :cols ["name" "domain" "content_type_id"]
                        :vals [["youtube" "www.youtube.com" 1]
                               ["spotify" "www.spotify.com" 2]
                               ["medium" "www.medium.com" 3]]})
    (master/seed-table {:table "sectors"
                        :cols ["name"]
                        :vals [["renewable energy"]
                               ["conservation ecology"]
                               ["recycling"]]})))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (master/drop-tables ds-master)))
