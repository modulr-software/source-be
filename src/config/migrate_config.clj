(ns config.migrate-config
  (:require
   [source.db.util :as db.util]
   [source.db.master.db-version :as version]))

(defn init [args]
  (let [ds (db.util/conn :master)]
    (version/create-table! ds)))

(defn current-version []
  (let [ds (db.util/conn :master)]
    (-> (version/get-latest-version ds)
        (:version)
        (or 0))))

(defn update-version [version]
  (let [ds (db.util/conn :master)]
    (version/insert-version! ds {:version version})))

(defn migrate-config []
  {:directory "/src/migrations"
   :init init
   :current-version current-version
   :update-version update-version})
