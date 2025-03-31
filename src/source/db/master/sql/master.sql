-- src/source/db/master/sql/master.sql

-- :name table :? :1
-- :doc gets the table record for a given table name
select * from sqlite_master where type = 'table' and name = :name

-- :name tables ?: :*
-- :doc gets all table records from sqlite_master
select * from sqlite_master where type = 'table' and name != 'sqlite_sequence'

-- :name drop-table :! :n
-- :doc drops a table with a given name if it exists
drop table if exists :i:table

-- :name num-records :? :1
-- :doc gets the number of records on a given table name
select count(*) as count from :i:table

-- :name seed-table :! :n
-- :doc seeds initial table data
insert into :i:table (:i*:cols) values :t*:vals