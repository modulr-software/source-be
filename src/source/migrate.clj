(ns source.migrate
  (:require
   [k16.mallard :as mallard]
   [k16.mallard.store.sqlite :as store]
   [k16.mallard.loader.fs :as loader.fs]
   [source.db.util :as db.util]))

(def ^:private migrations
  (loader.fs/load! "src/source/migrations"))

(defn- create-context []
  (let [db (db.util/conn :master)]
    {:db-master db}))

(defn run-migrations [args]
  (let [context (create-context)
        datastore (store/create-datastore
                   {:db (:db-master context)
                    :table-name "migrations"})]
    (mallard/run {:context context
                  :store datastore
                  :operations migrations}
                 args)))

(defn -main [& args]
  (run-migrations args))
