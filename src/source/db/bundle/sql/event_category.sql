-- src/source/db/bundle/sql/event_category.sql

-- :name create-event-category-join-table :! :n
-- :doc creates the event-category-join table
create table if not exists event_category (
    id integer primary key autoincrement,
    event_id integer not null,
		category_id text not null,
    foreign key (event_id) references analytics(id)
)

-- :name drop-event-category-table :! :n
-- :doc drops the whole event-category table
drop table if exists event_category

-- :name select-all-event-category :? :*
-- :doc gets all records from event-category table
select * from event_category

-- :name insert-event-category :! :n
-- :doc insert an event into the event-category table
insert into event_category (:i*:cols) values :t*:vals


