-- src/source/db/master/sql/feeds_sectors.sql

-- :name create-table! :! :n
-- :doc create the feeds_sectors table
create table if not exists feeds_sectors (
	id integer primary key autoincrement,
	feed_id integer not null,
	sector_id integer not null,
	foreign key (feed_id) references feeds(id),
	foreign key (sector_id) references sectors(id)
)

-- :name drop-table! :! :n
-- :doc drop the whole feeds_sectors table
drop table if exists feeds_sectors

-- :name select-all :? :*
-- :doc gets all feeds_sectors from the feeds_sectors table
select * from feeds_sectors

-- :name insert-feeds-sectors! :! :n
-- :doc insert a feed sector record
insert into feeds_sectors (:i*:cols) values (:v*:vals)

-- :name select-by-feed-id :? :*
-- :doc select all feeds sectors records with a given feed id
select * from feeds_sectors where feed_id = :feed-id

