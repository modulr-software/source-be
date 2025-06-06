(ns migrations.002-another-test
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

(defn run-up! [context]
  (jdbc/execute! (:db context) [users-table]))

(defn run-down! [context]
  (jdbc/execute! (:db context) ["DROP TABLE users"]))
