(ns source.migrations.001-init-master-db
  (:require [source.db.master.users :as users]
            [source.db.master.bundles :as bundles]
            [source.db.master.baselines :as baselines]
            [source.db.master.cadences :as cadences]
            [source.db.master.feeds :as feeds]
            [source.db.master.feeds-categories :as feeds-categories]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.categories :as categories]
            [source.db.master.content-type :as content-types]
            [clojure.data.json :as json]
            [source.db.master.core :as master]
            [source.config :as conf]))

(defn read-admins []
  (try
    (-> :admins-path
        (conf/read-value)
        (slurp)
        (json/read-json))
    (catch Exception e
      (println (str "Couldn't read the admins file: " (.getMessage e))))))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (users/create-users-table ds-master)
    (bundles/create-bundles-table ds-master)
    (baselines/create-baselines-table ds-master)
    (cadences/create-cadences-table ds-master)
    (feeds/create-table! ds-master)
    (feeds-categories/create-table! ds-master)
    (providers/create-providers-table ds-master)
    (sectors/create-sectors-table ds-master)
    (categories/create-table ds-master)
    (content-types/create-content-types-table ds-master)
    (master/seed-table ds-master {:table "baselines"
                                  :cols ["label" "min" "max"]
                                  :vals [["0-1000" 0 1000]
                                         ["1000-10000" 1000 10000]
                                         ["10000-100000" 10000 100000]
                                         ["100000-1000000" 100000 1000000]]})
    (master/seed-table ds-master {:table "cadences"
                                  :cols ["label" "days"]
                                  :vals [["daily" 1]
                                         ["weekly" 7]
                                         ["biweekly" 14]
                                         ["monthly" 30]]})
    (master/seed-table ds-master {:table "content_types"
                                  :cols ["name"]
                                  :vals [["video"]
                                         ["podcast"]
                                         ["blog"]]})
    (master/seed-table ds-master {:table "providers"
                                  :cols ["name" "domain" "content_type_id"]
                                  :vals [["youtube" "www.youtube.com" 1]
                                         ["spotify" "www.spotify.com" 2]
                                         ["medium" "www.medium.com" 3]]})
    (master/seed-table ds-master {:table "sectors"
                                  :cols ["name"]
                                  :vals [["renewable energy"]
                                         ["conservation ecology"]
                                         ["recycling"]]})

    (doseq [{:keys [email password]} (read-admins)]
      (master/seed-table ds-master {:table "users"
                                    :cols ["email" "password" "type"]
                                    :vals [[email password "admin"]]}))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (master/drop-tables ds-master)))

(comment 
  (read-admins))
