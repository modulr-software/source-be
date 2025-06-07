-- src/source/db/master/sql/bundles.sql

-- :name create-bundles-table :! :n
-- :doc creates the bundles table
create table if not exists bundles (
    id integer primary key autoincrement,
    user_id integer,
    video integer not null default 0,
    podcast integer not null default 0,
    blog integer not null default 0,
    hash text,
    foreign key (user_id) references users(id)
)

-- :name drop-bundles-table :! :n
-- :doc drops the whole bundles table
drop table if exists bundles

-- :name select-all-bundles :? :*
-- :doc gets all bundles from the bundles table
select * from bundles

-- :name bundle
-- :doc select a bundle

-- :name insert-bundle :! :n
-- :doc insert a bundle into the bundles table
insert into bundles (:i*:cols) values (:v*:vals)


