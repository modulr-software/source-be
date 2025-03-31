-- src/source/db/master/sql/content_type.sql

-- :name create-content-types-table :! :n
-- :doc creates the content types table
create table if not exists content_types (
    id integer primary key autoincrement,
    name text
)

-- :name drop-content-types-table :! :n
-- :doc drop the whole content types table
drop table if exists content_types

-- :name content-types :? :*
-- :doc selects all content types
select * from content_types


-- :name content-type :? :1
-- :doc selects a single content type by id
select * from content_types where id = :id

-- :name insert-content-type :! :n
-- :doc create a single content type
insert into content_types (name) values (:name)