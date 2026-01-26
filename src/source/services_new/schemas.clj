(ns source.services-new.schemas
  (:require [malli.util :as mu]))

(def Business
  [:map
   [:id :int]
   [:name :string]
   [:address :string]
   [:url :string]
   [:linkedin :string]
   [:twitter :string]])

(def User
  [:map
   [:id :int]
   [:email :string]
   [:firstname :string]
   [:lastname :string]
   [:type [:enum ["creator" "distributor" "admin"]]]
   [:email-verified :int]
   [:onboarded :int]
   [:address :string]
   [:mobile :string]
   [:profile-image :string]])

(def UserWithBusiness
  (-> User
      (mu/assoc :business Business)))

(def AuthSchema
  [:map
   [:access-token :string]
   [:refresh-token :string]])

(def Login
  (-> AuthSchema
      (mu/assoc :user User)))

(def Register
  (-> AuthSchema
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

(def Feed
  [:map
   [:id :int]
   [:title :string]
   [:display-picture :string]
   [:url :string]
   [:rss-url :string]
   [:provider-id :int]
   [:created-at :string]
   [:updated-at :string]
   [:ts-and-cs :int]
   [:state [:enum ["live" "not live" "pending"]]]])

(def FeedWithChildren
  (-> Feed
      (mu/assoc :user User)
      (mu/assoc :content-type ContentType)
      (mu/assoc :cadence Cadence)
      (mu/assoc :baseline Baseline)))

(def IncomingPost
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

(def IncomingPostWithChildren
  (-> IncomingPost
      (mu/assoc :feed FeedWithChildren)))

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

(def Job
  [:map
   [:id :int]
   [:job-id :string]
   [:status [:enum ["running" "stopped"]]]
   [:args :string]
   [:handler :string]
   [:last-heartbeat :string]])

(def JobMetadata
  [:map
   [:initial-delay :int]
   [:auto-start :int]
   [:stop-after-fail :int]
   [:kill-after :int]
   [:num-calls :int]
   [:interval :int]
   [:recurring :int]
   [:created-at :string]
   [:sleep :int]])

(def JobWithMetadata
  (-> Job
      (mu/assoc :job-metadata JobMetadata)))

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

(def GeneralAnalytics
  [:vector
   [:map
    [:day :string]
    [:impressions :int]
    [:clicks :int]
    [:views :int]]])

(def DeltasAnalytics
  [:vector
   [:map
    [:week :string]
    [:impressions :float]
    [:clicks :float]
    [:views :float]]])

(def TopAnalytics
  [:vector
   [:map
    [:top :string]
    [:impressions :int]
    [:clicks :int]
    [:views :int]]])

(def AverageEngagementAnalytics
  [:map
   [:average :float]])
