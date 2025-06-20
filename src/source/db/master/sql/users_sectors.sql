-- src/source/db/master/sql/users_sectors.sql

-- :name create-table! :! :n
-- :doc create the users_sectors table
create table if not exists users_sectors (
	id integer primary key autoincrement,
	user_id integer not null,
	sector_id integer not null,
	foreign key (user_id) references users(id),
	foreign key (sector_id) references sectors(id)
)

-- :name drop-table! :! :n
-- :doc drop the whole users_sectors table
drop table if exists users_sectors

-- :name select-all :? :*
-- :doc gets all users_sectors from the users_sectors table
select * from users_sectors

-- :name insert-users-sectors! :! :n
-- :doc insert a user sector record
insert into users_sectors (:i*:cols) values (:v*:vals)

-- :name select-by-user-id :? :*
-- :doc select all users sectors records with a given user id
select * from users_sectors where user_id = :user-id

