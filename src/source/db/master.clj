(ns source.db.master
  (:require [source.db.tables :as tables]))

(def users
  (tables/create-table-sql
   :users
   (tables/table-id)
   [:email :text]
   [:password :text]
   [:firstname :text]
   [:lastname :text]
   [:type :text [:check [:in :type ["creator" "distributor" "admin"]]]]
   [:email-verified :integer [:default 0]]
   [:onboarded :integer [:default 0]]
   [:address :text]
   [:mobile :text]
   [:profile-image :text]))

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
   [:uuid :text :not nil :unique]
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

(def feed-categories
  (tables/create-table-sql
   :feed-categories
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
   [:linkedin :text [:default nil]]
   [:twitter :text [:default nil]]))

(def user-sectors
  (tables/create-table-sql
   :user-sectors
   (tables/table-id)
   [:user-id :integer :not nil]
   [:sector-id :integer :not nil]
   (tables/foreign-key :user-id :users :id)
   (tables/foreign-key :sector-id :sectors :id)))

(def feed-sectors
  (tables/create-table-sql
   :feed-sectors
   (tables/table-id)
   [:feed-id :integer :not nil]
   [:sector-id :integer :not nil]
   (tables/foreign-key :feed-id :feeds :id)
   (tables/foreign-key :sector-id :sectors :id)))

(def selection-schemas
  (tables/create-table-sql
   :selection-schemas
   (tables/table-id)
   [:version :integer :not nil]
   [:output-schema-id :integer :not nil]
   [:provider-id :integer :not nil]
   (tables/foreign-key :provider-id :providers :id)))

(comment
  (require '[honey.sql :as sql])

  (sql/format users)
  (sql/format sectors)
  (sql/format content-types)
  (sql/format cadences)
  (sql/format categories)
  (sql/format baselines)
  (sql/format bundles)
  (sql/format feeds)
  (sql/format feed-categories)
  (sql/format providers)
  (sql/format businesses)
  (sql/format user-sectors)
  (sql/format feed-sectors)
  (sql/format selection-schemas)
  ())
