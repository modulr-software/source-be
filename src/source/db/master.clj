(ns source.db.master
  (:require [source.db.honey :as hon]
            [honey.sql :as sql]
            [honey.sql.helpers :as hsql]
            [source.db.tables :as tables]
            [source.db.master.providers :as providers]))

;;PUT ALL YO TABLE DEFINITIONS HERE SO MIGRATIONS CAN REFERENCE THEM
(def users
  (tables/create-table-sql
   :users
   (tables/table-id)
   [:email :text]
   [:password :text]
   [:sector-id :integer [:default nil]]
   [:firstname :text]
   [:lastname :text]
   [:type :text [:check [:in :type ["provider" "distributor" "admin"]]]]
   [:email-verified :integer [:default 0]]
   [:onboarded :integer [:default 0]]
   [:address :text]
   [:mobile :text]
   [:profile-image :text]
   (tables/foreign-key :sector-id :sectors :id)))

(def sectors
  (tables/create-table-sql
   :sectors
   (tables/table-id)
   [:name :text]))

(def categories
  (tables/create-table-sql
   :categories
   (tables/table-id)
   [:name :text]))

(def content-types
  (tables/create-table-sql
   :content-types
   (tables/table-id)
   [:name :text]))

(def cadences
  (tables/create-table-sql
   :cadences
   (tables/table-id)
   [:label :text]
   [:days :integer]))

(def baselines
  (tables/create-table-sql
   :baselines
   (tables/table-id)
   [:label :text]
   [:min :integer]
   [:max :integer]))

(def bundles 
  (tables/create-table-sql
    :bundles
    (tables/table-id)
    [:user-id :integer]
    [:video :integer :not :null [:default 0]]
    [:podcast :integer :not :null [:default 0]]
    [:blog :integer :not :null [:default 0]]
    [:hash :text]
    (tables/foreign-key :user-id :users :id)))

(def feeds 
  (tables/create-table-sql
    :feeds
    (tables/table-id)
    [:title :text :not :null]
    [:display-picture :text]
    [:url :text]
    [:rss-url :text :not :null]
    [:user-id :integer]
    [:provider-id :integer]
    [:created-at :datetime :not :null]
    [:updated-at :datetime]
    [:content-type-id :integer :not :null]
    [:cadence-id :integer :not :null]
    [:baseline-id :integer :not :null]
    [:ts-and-cs :text]
    [:state :text]
    (tables/foreign-key :user-id :users :id)
    (tables/foreign-key :provider-id :providers :id)
    (tables/foreign-key :cadence-id :cadences :id)
    (tables/foreign-key :baseline-id :baselines :id)
    (tables/foreign-key :content-type-id :content-types :id)))

(def feeds-categories
  (tables/create-table-sql
    :feeds-categories
    (tables/table-id)
    [:feed-id :integer :not :null]
    [:category-id :integer :not :null]
    (tables/foreign-key :feed-id :feeds :id)
    (tables/foreign-key :category-id :categories :id)))

(def providers
  (tables/create-table-sql
    :providers
    (tables/table-id)
    [:name :text]
    [:domain :text]
    [:content-type-id :integer]
    (tables/foreign-key :content-type-id :content-types :id)))

(comment 
  (sql/format users)
  (sql/format sectors)
  (sql/format content-types)
  (sql/format cadences)
  (sql/format categories)
  (sql/format baselines)
  (sql/format bundles)
  (sql/format feeds)
  (sql/format feeds-categories)
  (sql/format providers)
  ())
