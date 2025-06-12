-- src/source/db/master/sql/feeds.sql

-- :name create-table! :! :n
-- :doc creates the feeds table
create table if not exists feeds (
    id integer primary key autoincrement,
    title text not null,
    display_picture text,
    url text,
    rss_url text not null,
    user_id integer,
    provider_id integer,
    created_at datetime not null,
    updated_at datetime,
    content_type_id integer not null,
    cadence_id integer not null,
    baseline_id integer not null,
    ts_and_cs text,
    state text,
    foreign key (user_id) references users(id),
    foreign key (provider_id) references providers(id),
    foreign key (cadence_id) references cadences(id),
    foreign key (baseline_id) references baselines(id),
		foreign key (content_type_id) references content_types(id)
);

-- :name drop-table! :! :n
-- :doc drops the whole feeds table
drop table if exists feeds;

-- :name select-all :? :*
-- :doc gets all feeds from the feeds table
select * from feeds;

-- :name insert-feed! :! :n
-- :doc inserts a feed into the feeds table
insert into feeds (:i*:cols) values (:v*:vals);

-- :name select-by-id :? :1
-- :doc gets a single feed by id
select * from feeds where id = :id;
