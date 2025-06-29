(ns source.db.bundle 
  (:require [source.db.tables :as tables]
            [honey.sql :as sql]))

(def event-categories
  (tables/create-table-sql
    :event-categories
    (tables/table-id)
    [:event-id :integer :not nil]
    [:category-id :text :not nil]
    (tables/foreign-key :event-id :analytics :id)))

(def outgoing-posts 
  (tables/create-table-sql
    :outgoing-posts
    (tables/table-id)
    [:title :text]
    [:subtitle :text]
    [:stream-url :text [:default nil]]
    [:content-type :text [:default nil]]
    [:feed-id :integer]
    [:creator-id :integer]
    (tables/foreign-key :creator-id :users :id)
    (tables/foreign-key :feed-id :feeds :id)))

(def analytics 
  (tables/create-table-sql
    :analytics
    (tables/table-id)
    [:post-id :integer :not nil]
    [:event-type :text :not nil]
    [:timestamp :text :not nil]))

(comment
  (sql/format event-categories)
  (sql/format outgoing-posts)
  (sql/format analytics)
  ())

