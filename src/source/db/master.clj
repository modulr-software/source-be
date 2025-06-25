(ns source.db.master
  (:require [source.db.honey :as hon]
            [honey.sql :as sql]
            [honey.sql.helpers :as hsql]
            [source.db.tables :as tables]
            [source.db.master.users-sectors :as users-sectors]
            [source.db.master.feeds-sectors :as feeds-sectors]))

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
   [:video :integer :not nil [:default 0]]
   [:podcast :integer :not nil [:default 0]]
   [:blog :integer :not nil [:default 0]]
   [:hash :text]
   (tables/foreign-key :user-id :users :id)))

(def feeds
  (tables/create-table-sql
   :feeds
   (tables/table-id)
   [:title :text :not nil]
   [:display-picture :text]
   [:url :text]
   [:rss-url :text :not nil]
   [:user-id :integer]
   [:provider-id :integer]
   [:created-at :datetime :not nil]
   [:updated-at :datetime]
   [:content-type-id :integer :not nil]
   [:cadence-id :integer :not nil]
   [:baseline-id :integer :not nil]
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
   [:feed-id :integer :not nil]
   [:category-id :integer :not nil]
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

(def businesses
  (tables/create-table-sql
   :businesses
   (tables/table-id)
   [:name :text]
   [:url :text [:default nil]]
   [:sector-id :integer [:default nil]]
   (tables/foreign-key :sector-id :sectors :id)))

(def users-sectors
  (tables/create-table-sql
    :users-sectors
    (tables/table-id)
    [:user-id :integer :not nil]
    [:sector-id :integer :not nil]
    (tables/foreign-key :user-id :users :id)
    (tables/foreign-key :sector-id :sectors :id)))

(def feeds-sectors
  (tables/create-table-sql
    :feeds-sectors
    (tables/table-id)
    [:feed-id :integer :not nil]
    [:sector-id :integer :not nil]
    (tables/foreign-key :feed-id :feeds :id)
    (tables/foreign-key :sector-id :sectors :id)))

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
  (sql/format businesses)
  (sql/format users-sectors)
  (sql/format feeds-sectors)
  ())
