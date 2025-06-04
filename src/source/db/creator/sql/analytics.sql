
-- src/source/db/creator/sql/analytics.sql

-- :name create-analytics-table :! :n
-- :doc creates the analytics table
create table if not exists analytics (
    id integer primary key autoincrement,
    post_id integer not null,
		event_type text not null,
		timestamp text not null
)

-- :name drop-analytics-table :! :n
-- :doc drops the whole analytics table
drop table if exists analytics

-- :name select-all-analytics :? :*
-- :doc gets all analytics from the analytics table
select * from analytics

-- :name insert-event :<!
-- :doc insert an event into the analytics table
insert into analytics (post_id, event_type, timestamp)
values (:post_id, :event_type, :timestamp) returning *


