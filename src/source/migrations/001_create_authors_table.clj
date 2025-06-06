(ns migrations.001-create-authors-table
  (:require [next.jdbc :as jdbc]))

(def ^:private ^:sql users-table
  "CREATE TABLE if not exists users (
    id              integer primary key autoincrement,
    email           text,
    password        text,
    sector_id       integer default null,
    firstname       text,
    lastname        text,
    business_name   text,
    type            text,
    email_verified  integer default 0,
    onboarded       integer default 0,
    address         text default null,
    mobile          text default null,
    profile_image   text default null,
    foreign key (sector_id) references sector(id)
    )")

(def ^:private ^:sql baselines-table
  "create table if not exists baselines (
    id integer primary key autoincrement,
    label text,
    min integer,
    max integer
    )")

(def ^:private ^:sql bundles-table
  "create table if not exists bundles (
    id integer primary key autoincrement,
    user_id integer,
    video integer not null default 0,
    podcast integer not null default 0,
    blog integer not null default 0,
    hash text,
    foreign key (user_id) references users(id)
    )")

(def ^:private ^:sql cadences-table
  "create table if not exists cadences (
    id integer primary key autoincrement,
    label text,
    days integer
    )")

(def ^:private ^:sql categories-table
  "create table if not exists categories (
    id integer primary key autoincrement,
		name text not null
    )")

(def ^:private ^:sql content-types-table
  "create table if not exists content_types (
    id integer primary key autoincrement,
    name text
    )")

(def ^:private ^:sql feeds-table
  "create table if not exists feeds (
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
    );")

(defn run-up! [context]
  (jdbc/execute! (:db context) [users-table]))

(defn run-down! [context]
  (jdbc/execute! (:db context) ["DROP TABLE users"]))
