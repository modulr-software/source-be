-- src/source/db/master/sql/users.sql

-- :name create-users-table
-- :command :execute
-- :result :raw
-- :doc create users table
create table if not exists users (
    id              integer primary key autoincrement,
    email           text,
    password        text,
    sector_id       integer default null,
    firstname       text,
    lastname        text,
    type            text check(type in ('provider', 'distributor', 'admin')) not null default 'distributor',
    email_verified  integer default 0,
    onboarded       integer default 0,
    address         text default null,
    mobile          text default null,
    profile_image   text default null,
    foreign key (sector_id) references sector(id)
)

-- :name drop-users-table
-- :command :execute
-- :result :raw
-- :doc drop the users table
drop table if exists users;

-- :name insert-user :! :n
-- :doc Insert a single character
insert into users (email, password, firstname, lastname, type) values (:email, :password, :firstname, :lastname, :type)

-- :name insert-into-users :! :n
-- :doc Insert a given set of values into given columns
insert into users (:i*:cols) values (:v*:vals)

-- :name update-user! :! :n 
-- :doc update values in the users table
update users 
set (:i*:cols) = :tuple:vals
where id = :id

-- :name user :? :1
-- :doc query for a single user by id
select * from users where id = :id

-- :name user-by :? :1
-- :doc get a user by a given column with given value
select * from users where :i:col = :val

-- :name users :? :*
-- :doc get all users
select * from users

-- :name find-users-by-name :? :*
-- :doc get all users with name like given name
select * from users where firstname like :name-like or lastname like :name-like
