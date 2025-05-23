-- src/source/db/master/sql/cadences.sql

-- :name create-cadences-table :! :n
-- :doc create the cadences table
create table if not exists cadences (
    id integer primary key autoincrement,
    label text,
    days integer
)

-- :name drop-cadences-table :! :n
-- :doc drops the whole cadences table
drop table if exists cadences

-- :name insert-cadence :! :n
-- :doc creates a new cadence
insert into cadences (label, days) values (:label, :days)

-- :name cadences :? :*
-- :doc gets all cadences
select * from cadences

-- :name cadence :? :1
-- :doc gets a single cadence by id
select * from cadences where id = :id