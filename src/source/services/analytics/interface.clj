(ns source.services.analytics.interface
  (:require [source.services.analytics.core :as core]))

(defn metric-query
  "Generic select query function for returning analytics data from the events table"
  [ds {:keys [_select _order-by _group-by _metric _feed-id _post-id _content-type-id _bundle-id _creator-id _distributor-id _min-date _max-date _category-ids _ret] :as opts}]
  (core/metric-query ds opts))

(defn statistics-query
  "returns the number of impressions, clicks and views, filtered by any other arguments accepted by metric-query"
  [ds opts]
  (core/statistics-query ds opts))

(defn interval-statistics-query
  "returns the number of impressions, clicks and views per interval (:daily, :weekly, :monthly or :yearly) over the given time period, 
  filtered by any other arguments accepted by metric-query.
  
  Date parameters must be in the format YYYY-mm-dd."
  [ds interval min-date max-date opts]
  (core/interval-statistics-query ds interval min-date max-date opts))

(defn weekly-growth-averages
  "Returns the percentage of growth in impressions, clicks and views per week, over the given time period. 
  Uses the first week as a basis for comparison, not included in results.
  Can be filtered by any other arguments accepted by metric-query."
  [ds min-date max-date opts]
  (core/weekly-growth-averages ds min-date max-date opts))

(defn average-engagement
  "Returns the average engagement (average clicks and views) over the given time period.
  Can be filtered by any other arguments accepted by metric-query."
  [ds min-date max-date opts]
  (core/average-engagement ds min-date max-date opts))

(defn click-through-rate
  "Returns the click-through rate based on impressions and clicks filtered by any arguments accepted by metric-query"
  [ds opts]
  (core/click-through-rate ds opts))

(defn insert-event [ds {:keys [_data _ret] :as opts}]
  (core/insert-event ds opts))

(defn insert-event-categories [ds {:keys [_data _ret] :as opts}]
  (core/insert-event-categories ds opts))

(defn insert-feed-event-categories
  "Given a list of events and a list of feeds (or a single event/feed), 
  inserts an event category record for each event and each category 
  associated with the given feeds."
  [ds events feeds]
  (core/insert-feed-event-categories ds events feeds))

(defn insert-post-event-categories
  "Given a list of events and a list of posts (or a single event/post),
  inserts an event category record for each event and each category 
  associated with the given posts"
  [ds events posts]
  (core/insert-post-event-categories ds events posts))

(defn insert-feed-impressions
  "Given a list of feeds and a bundle id, inserts impression event reconds 
  for each given feed. Inserts event categories for each feed."
  [ds feeds bundle-id]
  (core/insert-feed-impressions ds feeds bundle-id))

(defn insert-post-impressions
  "Given a list of posts and a bundle id, inserts impression event reconds 
  for each given post. Inserts event categories for each post."
  [ds posts bundle-id]
  (core/insert-post-impressions ds posts bundle-id))

(defn insert-feed-click
  "Given a feed and a bundle id, inserts a click event record 
  for the given feed"
  [ds {:keys [_id _content-type-id _user-id] :as feed} bundle-id]
  (core/insert-feed-click ds feed bundle-id))

(defn insert-post-click
  "Given a post and a bundle id, inserts a click event record 
  for the given post"
  [ds {:keys [_id _feed-id _content-type-id _creator-id] :as post} bundle-id]
  (core/insert-post-click ds post bundle-id))

(defn insert-post-view
  "Given a post and a bundle id, inserts a view event record 
  for the given post"
  [ds {:keys [_id _feed-id _content-type-id _creator-id] :as post} bundle-id]
  (core/insert-post-view ds post bundle-id))
