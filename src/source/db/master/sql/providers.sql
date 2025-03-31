-- src/source/db/master/sql/providers.sql

-- :name create-providers-table :! :n
-- :doc creates the providers table
create table if not exists providers (
    id integer primary key autoincrement,
    name text,
    domain text,
    content_type_id integer,
    foreign key (content_type_id) references content_types(id)
)

-- :name drop-providers-table :! :n
-- :doc drop the whole providers table 
drop table if exists providers

-- :name insert-provider :! :n
-- :doc create a new provider
insert into providers (name, domain, content_type_id) values (:name, :domain, :content-type-id)

-- :name providers :? :*
-- :doc select all providers
select * from providers

-- :name provider ?: :1
-- :doc select a provider with a given id if exists
select * from providers where id = :id