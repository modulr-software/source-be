-- src/source/db/master/sql/sectors.sql

-- :name create-sectors-table
-- :doc create the sectors table
create table if not exists sectors (
    id integer primary key autoincrement,
    name text
)

-- :name drop-sectors-table
-- :doc drop the whole sectors table
drop table if exists sectors

-- :name insert-sector :i :n
-- :doc insert a new sector record
insert into sectors (name) values (:name)

-- :name sectors :? :*
-- :doc get a list of all sectors
select * from sectors

-- :name sector :? :1
-- :doc gets a single sector by id
select * from sectors where id = :id