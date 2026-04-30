(ns source.migrations.019-password-hash
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :users)
         (hsql/add-column :password-hash :text)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :users)
         (hsql/drop-column :password-hash)))))
