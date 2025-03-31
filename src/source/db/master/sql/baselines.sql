-- src/source/db/master/sql/baselines.sql

-- :name create-baselines-table :! :n
-- :doc creates the basedlines table
create table if not exists baselines (
    id integer primary key autoincrement,
    label text,
    min integer,
    max integer
)

-- :name drop-baselines-table :! :n
-- :doc drop the whole baselines table
drop table if exists baselines

-- :name insert-baseline :! :n
-- :doc create a new baseline
insert into baselines (label, min, max) values (:label, :min, :max)

-- :name baselines :? :*
-- :doc select all baselines
select * from baselines

-- :name baseline ?: :1
-- :doc select a baseline with a given id if exists
select * from baselines where id = :id