(ns source.workers.schemas
  (:require [malli.util :as mu]
            [source.routes.openapi :as api]))

(def Business
  [:map
   [:id :int]
   [:name :string]
   [:address :string]
   [:url :string]
   [:linkedin :string]
   [:twitter :string]])

(def UserType [:enum ["creator" "distributor" "admin"]])

(def User
  [:map
   [:id :int]
   [:email :string]
   [:firstname :string]
   [:lastname :string]
   [:type UserType]
   [:email-verified :int]
   [:onboarded :int]
   [:address :string]
   [:mobile :string]
   [:profile-image :string]])

(def UserWithBusiness
  (-> User
      (mu/assoc :business Business)))

(def SessionCredentials
  [:map
   [:access-token :string]
   [:refresh-token :string]])

(def Login
  (-> SessionCredentials
      (mu/assoc :user User)))

(def Register
  (-> SessionCredentials
      (mu/assoc :user User)))

(def ConstantSchema
  [:map
   [:id :int]
   [:name :string]])

(def Sector
  ConstantSchema)

(def Category
  (-> ConstantSchema
      (mu/assoc :display-picture :string)))

(def Categories
  [:vector Category])

(def ContentType
  ConstantSchema)

(def ContentTypes
  [:vector ContentType])

(def Provider
  (-> ConstantSchema
      (mu/assoc :domain :string)))

(def ProviderWithContentType
  (-> Provider
      (mu/assoc :content-type ContentType)))

(def BusinessType
  ConstantSchema)

(def Cadence
  [:map
   [:id :int]
   [:label :string]
   [:days :int]])

(def Baseline
  [:map
   [:id :int]
   [:label :string]
   [:min :int]
   [:max :int]])

(def FeedStatus [:enum ["live" "not live" "pending"]])

(def FeedRecord
  [:map
   [:id :int]
   [:title :string]
   (api/sometimes :display-picture :string)
   (api/sometimes :description :string)
   (api/sometimes :url :string)
   [:rss-url :string]
   [:created-at :string]
   (api/sometimes :updated-at :string)
   (api/sometimes :ts-and-cs :int)
   [:state FeedStatus]])

(def Feed
  (-> FeedRecord
      (mu/assoc :user-id :int)
      (mu/assoc :content-type-id :int)
      (mu/assoc :cadence-id :int)
      (mu/assoc :baseline-id :int)
      (mu/assoc :provider-id :int)))

(def Feeds
  [:vector Feed])

(defn paginated [data-schema]
  [:map
   [:pagination [:map
                 [:page-size :int]
                 [:total-size :int]
                 [:current-index :int]
                 (api/sometimes :next-index :int)]]
   [:data data-schema]])

(def IncomingPostRecord
  [:map
   [:id :int]
   [:post-id :string]
   [:title :string]
   [:thumbnail :string]
   [:info :string]
   [:url :string]
   [:stream-url :string]
   [:season :int]
   [:episode :int]
   [:redacted :int]
   [:posted-at :string]])

(def IncomingPost
  (-> IncomingPostRecord
      (mu/assoc :feed FeedRecord)))

;; This is exactly the same as IncomingPost except without redacted
;; We could probably just make a Post schema with (sometimes :redacted :int)
(def OutgoingPost
  [:map
   [:id :int]
   [:post-id :string]
   [:title :string]
   [:thumbnail :string]
   [:info :string]
   [:url :string]
   [:stream-url :string]
   [:season :int]
   [:episode :int]
   [:posted-at :string]])

(def Post
  [:map
   [:id :int]
   [:post-id :string]
   [:feed-id :int]
   [:creator-id :int]
   [:content-type-id :int]
   [:title :string]
   [:thumbnail (api/maybe :string)]
   [:info (api/maybe :string)]
   [:url (api/maybe :string)]
   [:stream-url (api/maybe :string)]
   [:season (api/maybe :int)]
   [:episode (api/maybe :int)]
   (api/sometimes :redacted :int)
   [:posted-at (api/maybe :string)]])

(def Posts
  [:vector Post])

(def QueryUUID
  [:uuid {:description "Bundle UUID"} :string])

(def QueryStart
  [:start
   {:optional true
    :description "Used for pagination. Specifies the starting point for the returned items, incremented by the limit."}
   :int])

(def QueryLimit
  [:limit
   {:optional true
    :description "Used for pagination. Specifies a number of items to be returned."}
   :int])

(def QueryContentType
  [:type {:optional true
          :description "Filters by content type ID"} :int])

(def QueryLatest
  [:latest
   {:optional true
    :description "Filters by most recently published"}
   [:enum "true" "false"]])

(def JobStatus [:enum ["running" "stopped"]])

(def Job
  [:map
   [:id :int]
   [:job-id :string]
   [:status JobStatus]
   [:args :string]
   [:handler :string]
   [:last-heartbeat :string]])

(def JobMetadata
  [:map
   [:id :string]
   [:initial-delay :int]
   [:stop-after-fail :int]
   (api/sometimes :auto-start :int)
   (api/sometimes :kill-after :int)
   [:num-calls :int]
   [:interval :int]
   [:recurring :int]
   (api/sometimes :created-at :string)
   [:sleep :int]])

(def JobWithMetadata
  (-> Job
      (mu/assoc :job-metadata JobMetadata)))

(def JobsWithMetadata
  [:vector JobWithMetadata])

(def Bundle
  [:map
   [:id :int]
   [:name :string]
   [:uuid :string]
   [:video :int]
   [:podcast :int]
   [:blog :int]
   [:hash :string]
   [:ts-and-cs :int]])

(def BundleWithUser
  (-> Bundle
      (mu/assoc :user User)))

(def GeneralStatistic
  [:map
   [:day :string]
   [:impressions :int]
   [:clicks :int]
   [:views :int]])

(def GeneralAnalytics
  [:vector GeneralStatistic])

(def DeltasStatistic
  [:map
   [:week :string]
   [:impressions :float]
   [:clicks :float]
   [:views :float]])

(def DeltasAnalytics
  [:vector DeltasStatistic])

(def TopStatistic
  [:map
   [:top :string]
   [:impressions :int]
   [:clicks :int]
   [:views :int]])

(def TopAnalytics
  [:vector TopStatistic])

(def AverageEngagementAnalytics
  [:map
   [:average :float]])
