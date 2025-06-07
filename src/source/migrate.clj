(ns source.migrate
  (:require
   [k16.mallard :as mallard]
   [k16.mallard.store.sqlite :as store]
   [k16.mallard.loader.fs :as loader.fs]
   [source.db.util :as db.util]))

;; This is our interface for running migrations.
;;
;; Migrations will do the following:
;; - migrate or rollback updates on ALL affected dbs; when a migration is applied
;;   we want all of the databases and the generated schemas to stay in sync with eachother.
;; - seed the appropriate tables with data.
;; - (TODO) generate malli schemas to match the affected db schemas 

(def ^:private migrations
  (loader.fs/load! "src/source/migrations"))

(defn run-migrations [args]
  (let [context {:db-master (db.util/conn :master)}
        datastore (store/create-datastore
                   {:db (:db-master context)
                    :table-name "migrations"})]
    (mallard/run {:context context
                  :store datastore
                  :operations migrations}
                 args)))

(defn -main [& args]
  (run-migrations args))
