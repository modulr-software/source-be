(ns source.migrations.008-provider-rss-instructions
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :providers)
         (hsql/add-column :instructions :text)))

    (hon/execute!
     ds-master
     (-> (hsql/alter-table :providers)
         (hsql/add-column :placeholder-url :text)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :providers)
         (hsql/drop-column :instructions :placeholder-url)))))
