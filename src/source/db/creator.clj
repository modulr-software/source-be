(ns source.db.creator 
  (:require [honey.sql :as sql]
            [source.db.tables :as tables]))

(def event-category
  (tables/create-table-sql
    :event-category
    (tables/table-id)
    [:event-id :integer :not nil]
    [:category-id :text :not nil]
    (tables/foreign-key :event-id :analytics :id)))

(def analytics
  (tables/create-table-sql
    :analytics
    (tables/table-id)
    [:post-id :integer :not nil]
    [:event-type :text :not nil]
    [:timestamp :text :not nil]))

(comment 
  (sql/format event-category)
  (sql/format analytics)
  ())
