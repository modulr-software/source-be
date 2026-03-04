(ns source.migrations.013-email-hash
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/update! ds-master {:tname :users
                            :where [:= :type "admin"]
                            :data {:email-verified 1}})
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :users)
         (hsql/add-column :email-hash :text)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :users)
         (hsql/drop-column :email-hash)))))
