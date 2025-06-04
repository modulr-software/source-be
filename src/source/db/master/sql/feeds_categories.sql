-- src/source/db/master/sql/feeds_categories.sql

-- :name create-table :! :n
-- :doc created the feeds_categories table
create table if not exists feeds_categories (
	id integer primary key autoincrement,
	feed_id integer not null,
	category_id integer not null,
	foreign key (feed_id) references feeds(id),
	foreign key (category_id) references categories(id)
)

-- :name drop-table :! :n
-- :doc drop the whole feeds_categories table
drop table if exists feeds_categories

-- :name select-all :? :*
-- :doc gets all feeds_categories from the feeds_categories table
select * from feeds_categories

-- :name insert-feeds-categories :! :n
-- :doc insert a feed category record
insert into feeds_categories (:i*:cols) values (:v*:vals)

-- :name select-by-feed-id :? :*
-- :doc select all feeds categories records with a given feed id
select * from feeds_categories where feed_id = :feed-id
