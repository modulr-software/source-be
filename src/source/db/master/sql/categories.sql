-- src/source/db/master/sql/categories.sql

-- :name create-table :! :n
-- :doc creates the categories table
create table if not exists categories (
    id integer primary key autoincrement,
		name text not null
)

-- :name drop-table :! :n
-- :doc drops the whole categories table
drop table if exists categories

-- :name select-all :? :*
-- :doc gets all categories from the categories table
select * from categories

-- :name select-by-id :? :1
-- :doc get a single category by id
select * from categories where id = :id

-- :name insert :! :n
-- :doc insert a category into the categories table
insert into categories (:i*:cols) values (:v*:vals)


