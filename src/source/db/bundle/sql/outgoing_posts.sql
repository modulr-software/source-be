-- src/source/db/bundle/sql/outgoing_posts.sql

-- :name create-table! :! :n
-- :doc create the outgoing posts table
create table if not exists outgoing_posts (
    id integer primary key autoincrement,
    title text,
    subtitle text,
    stream_url text default null,
    content_type text default null,
		feed_id integer,
		creator_id integer,
    foreign key (creator_id) references users(id),
    foreign key (bundle_id) references bundles(id)
		foreign key (feed_id) references feeds(id)
)

-- :name select-outgoing-post-by-id :? :1
-- :doc get the user by the given post id
select * from outgoing_posts where id = :id

-- :name select-outgoing-posts-by-bundle-id :? :*
-- :doc gets all outgoing posts by bundle-id
select * from outgoing_posts where bundle_id = :bundle-id

-- :name insert-outgoing-post! :! :n
-- :doc insert an outgoing post for a bundle id
insert into outgoing_posts (bundle_id, title, subtitle, stream_url, content_type, creator_id, feed_id)
values (:bundle-id, :title, :subtitle, :stream-url, :content-type, :creator-id, :feed-id)

-- :name select-outgoing-posts-by-category :? :*
-- :doc select a set of outgoing psots for a bundle id by the content type
select * from outgoing_posts where bundle_id = :bundle-id and content_type = :content-type

-- :name count-outgoing-posts :? :1
-- :doc count the set of outgoing posts for given bundle id
select count(*) as count from outgoing_posts where bundle_id = :bundle-id

-- :name count-outgoing-posts-by-content-type :? :1
-- :doc count the set of outgoing posts for given bundle id and content type
select count(*) as count from outgoing_posts where bundle_id = :bundle-id and content_type = :content-type


-- :name drop-table! :! :n
-- :doc drop the whole outgoing_posts table
drop table if exists outgoing_posts
