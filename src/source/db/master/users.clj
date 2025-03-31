(ns source.db.master.users
  (:require [source.db.master.connection :refer [ds] :as c]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.password :as pw]))

(declare create-users-table)
(declare drop-users-table)
(declare insert-user)
(declare select-user-by-id)
(declare insert-user-columns)
(declare find-users-by-name)
(declare all-users)

(hugsql/def-db-fns "source/db/master/sql/users.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (create-users-table ds)
  (drop-users-table ds)
  (all-users ds)
  (insert-user ds {:email "merveillevaneck@gmail.com"
                           :password (pw/hash-password "test")
                           :firstname "merv"
                           :lastname "ilicious"
                           :business-name "modulr"
                           :type "creator" })
  (find-users-by-name ds {:name-like "%lici%"}))