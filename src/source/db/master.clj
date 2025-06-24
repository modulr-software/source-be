(ns source.db.master
  (:require [source.db.honey :as hon]
            [honey.sql :as sql]
            [honey.sql.helpers :as hsql]
            [source.db.tables :as tables]))

;;PUT ALL YO TABLE DEFINITIONS HERE SO MIGRATIONS CAN REFERENCE THEM
(def users
  (tables/create-table-sql
   :users
   (tables/table-id)
   [:email :text]
   [:password :text]
   [:sector_id :integer [:default nil]]
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

(sql/format baselines)




