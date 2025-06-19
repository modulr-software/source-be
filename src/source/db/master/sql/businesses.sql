-- src/source/db/master/sql/businesses.sql

-- :name create-table! :!
-- :command :execute
-- :result :raw
-- :doc create users table
create table if not exists businesses (
    id              integer primary key autoincrement,
    name  			text,
	url				text default null,
    sector_id       integer default null,
    foreign key (sector_id) references sector(id)
)

-- :name drop-table! :!
-- :command :execute
-- :result :raw
-- :doc drop the businesses table
drop table if exists businesses;

-- :name insert-business! :! :n
-- :doc Insert a single character
insert into businesses (name, url, sector_id) values (:name, :url, :sector_id)

