(ns source.services.analytics.core
  (:require [honey.sql.helpers :as hsql]
            [source.db.honey :as hon]
            [source.services.bundles :as bundles]
            [source.util :as util]
            [source.services.feed-categories :as feed-categories]))

(defn metric-query
  "Generic select query function for returning analytics data from the events table"
  [ds {:keys [select order-by group-by metric feed-id post-id content-type-id bundle-id creator-id distributor-id min-date max-date category-ids ret]}]
  (let [clauses (cond-> {}
                  (some? metric) (hsql/where [:= :event metric])
                  (some? feed-id) (hsql/where [:= :feed-id feed-id])
                  (some? post-id) (hsql/where [:= :post-id post-id])
                  (some? content-type-id) (hsql/where [:= :content-type-id content-type-id])
                  (some? bundle-id) (hsql/where [:= :bundle-id bundle-id])
                  (some? creator-id) (hsql/where [:= :creator-id creator-id])
                  (some? distributor-id) (hsql/where [:= :distributor-id distributor-id])
                  (and (some? min-date) (nil? max-date)) (hsql/where [:>= :timestamp min-date])
                  (and (some? max-date) (nil? min-date)) (hsql/where [:<= :timestamp max-date])
                  (and (some? min-date) (some? max-date)) (hsql/where [:between :timestamp min-date max-date])
                  (some? select) (merge select)
                  (some? order-by) (merge order-by)
                  (some? group-by) (merge group-by)
                  (seq category-ids) (-> (hsql/join [:event-categories :ec] [:= :events.id :ec.event-id])
                                         (hsql/where [:in :ec.category-id category-ids])))]
    (hon/execute!
     ds
     (merge {:select [[[:count :*] :total]]
             :from [:events]}
            clauses)
     {:ret (if ret ret :*)})))

(defn statistics-query
  "returns the number of impressions, clicks and views, filtered by any other arguments accepted by metric-query"
  [ds opts]
  (metric-query ds (merge {:select (hsql/select [(hsql/filter :%count.* (hsql/where := :event "impression")) :impressions]
                                                [(hsql/filter :%count.* (hsql/where := :event "click")) :clicks]
                                                [(hsql/filter :%count.* (hsql/where := :event "view")) :views])
                           :ret :1}
                          opts)))

(defn interval-statistics-query
  "returns the number of impressions, clicks and views per interval (:daily, :weekly, :monthly or :yearly) over the given time period, 
  filtered by any other arguments accepted by metric-query.
  
  Date parameters must be in the format YYYY-mm-dd."
  [ds interval min-date max-date opts]
  (let [select (cond
                 (= interval :daily) [[:date :timestamp] :day]
                 (= interval :weekly) [[:strftime "%W" :timestamp] :week]
                 (= interval :monthly) [[:strftime "%m" :timestamp] :month]
                 (= interval :yearly) [[:strftime "%Y" :timestamp] :year]
                 :else [[:date :timestamp] :day])
        column (cond
                 (= interval :daily) :day
                 (= interval :weekly) :week
                 (= interval :monthly) :month
                 (= interval :yearly) :year
                 :else :day)]

    (metric-query ds (merge {:select (hsql/select select
                                                  [(hsql/filter :%count.* (hsql/where := :event "impression")) :impressions]
                                                  [(hsql/filter :%count.* (hsql/where := :event "click")) :clicks]
                                                  [(hsql/filter :%count.* (hsql/where := :event "view")) :views])
                             :min-date (str min-date " 00:00:00")
                             :max-date (str max-date " 23:59:59")
                             :group-by (hsql/group-by column)
                             :order-by (hsql/order-by column)}
                            opts))))

(defn weekly-growth-averages
  "Returns the percentage of growth in impressions, clicks and views per week, over the given time period. 
  Uses the first week as a basis for comparison, not included in results.
  Can be filtered by any other arguments accepted by metric-query."
  [ds min-date max-date opts]
  (let [weeks (interval-statistics-query ds :weekly min-date max-date opts)
        {:keys [impressions clicks views]} (first weeks)]
    (mapv (fn [w]
            {:week (:week w)
             :impressions (float (* (/ (- (:impressions w) impressions) impressions) 100))
             :clicks (float (* (/ (- (:clicks w) clicks) clicks) 100))
             :views (float (* (/ (- (:views w) views) views) 100))})
          weeks)))

(defn average-engagement
  "Returns the average engagement (average clicks and views) over the given time period.
  Can be filtered by any other arguments accepted by metric-query."
  [ds min-date max-date opts]
  (let [{:keys [clicks views]} (statistics-query ds (merge {:min-date min-date
                                                            :max-date max-date}
                                                           opts))]
    (float (/ (+ clicks views) 2))))

(defn click-through-rate
  "Returns the click-through rate based on impressions and clicks filtered by any arguments accepted by metric-query"
  [ds opts]
  (let [{:keys [impressions clicks]} (statistics-query ds opts)]
    (float (* (/ clicks impressions) 100))))

(defn insert-event! [ds {:keys [data ret] :as opts}]
  (->> {:tname :events
        :data data
        :ret ret}
       (merge opts)
       (hon/insert! ds)))

(defn insert-event-categories! [ds {:keys [data ret] :as opts}]
  (->> {:tname :event-categories
        :data data
        :ret ret}
       (merge opts)
       (hon/insert! ds)))

(defn insert-feed-event-categories!
  "Given a list of events and a list of feeds (or a single event/feed), 
  inserts an event category record for each event and each category 
  associated with the given feeds."
  [ds events feeds]
  (let [multi-events? (vector? events)
        multi-feeds? (vector? feeds)
        events' (if multi-events? events [events])
        feeds' (if multi-feeds? feeds [feeds])
        category-ids (->> {:where [:in :feed-id (mapv :id feeds')]}
                          (feed-categories/feed-categories ds)
                          (mapv :category-id))
        event-categories (->> events'
                              (mapv (fn [{:keys [id]}]
                                      (mapv (fn [c]
                                              {:event-id id
                                               :category-id c})
                                            category-ids)))
                              (flatten)
                              (vec))]
    (insert-event-categories! ds {:data event-categories})))

(defn insert-post-event-categories!
  "Given a list of events and a list of posts (or a single event/post),
  inserts an event category record for each event and each category 
  associated with the given posts"
  [ds events posts]
  (let [multi-events? (vector? events)
        multi-posts? (vector? posts)
        events' (if multi-events? events [events])
        posts' (if multi-posts? posts [posts])
        feed-ids (mapv :feed-id posts')
        category-ids (->> {:where [:in :feed-id feed-ids]}
                          (feed-categories/feed-categories ds)
                          (mapv :category-id))
        event-categories (->> events'
                              (mapv (fn [{:keys [id]}]
                                      (mapv (fn [c]
                                              {:event-id id
                                               :category-id c})
                                            category-ids)))
                              (flatten)
                              (vec))]
    (insert-event-categories! ds {:data event-categories})))

(defn insert-feed-impressions!
  "Given a list of feeds and a bundle id, inserts impression event reconds 
  for each given feed. Inserts event categories for each feed."
  [ds feeds bundle-id]
  (let [bundle (bundles/bundle ds {:id bundle-id})
        events (mapv (fn [{:keys [id content-type-id user-id]}]
                       {:timestamp (util/get-utc-timestamp-string)
                        :event "impression"
                        :feed-id id
                        :content-type-id content-type-id
                        :creator-id user-id
                        :bundle-id bundle-id
                        :distributor-id (:user-id bundle)}) feeds)
        events' (insert-event! ds {:data events
                                  :ret :*})]
    (insert-feed-event-categories! ds events' feeds)))

(defn insert-post-impressions!
  "Given a list of posts and a bundle id, inserts impression event reconds 
  for each given post. Inserts event categories for each post."
  [ds posts bundle-id]
  (let [bundle (bundles/bundle ds {:id bundle-id})
        events (mapv (fn [{:keys [id feed-id content-type-id creator-id]}]
                       {:timestamp (util/get-utc-timestamp-string)
                        :event "impression"
                        :feed-id feed-id
                        :post-id id
                        :content-type-id content-type-id
                        :creator-id creator-id
                        :bundle-id bundle-id
                        :distributor-id (:user-id bundle)}) posts)
        events' (insert-event! ds {:data events
                                  :ret :*})]
    (insert-post-event-categories! ds events' posts)))

(defn insert-feed-click!
  "Given a feed and a bundle id, inserts a click event record 
  for the given feed"
  [ds {:keys [id content-type-id user-id] :as feed} bundle-id]
  (let [bundle (bundles/bundle ds {:id bundle-id})
        event {:timestamp (util/get-utc-timestamp-string)
               :event "click"
               :feed-id id
               :content-type-id content-type-id
               :creator-id user-id
               :bundle-id bundle-id
               :distributor-id (:user-id bundle)}
        event' (insert-event! ds {:data event
                                 :ret :*})]
    (insert-feed-event-categories! ds event' feed)))

(defn insert-post-click!
  "Given a post and a bundle id, inserts a click event record 
  for the given post"
  [ds {:keys [id feed-id content-type-id creator-id] :as post} bundle-id]
  (let [bundle (bundles/bundle ds {:id bundle-id})
        event {:timestamp (util/get-utc-timestamp-string)
               :event "click"
               :feed-id feed-id
               :post-id id
               :content-type-id content-type-id
               :creator-id creator-id
               :bundle-id bundle-id
               :distributor-id (:user-id bundle)}
        event' (insert-event! ds {:data event
                                 :ret :*})]
    (insert-post-event-categories! ds event' post)))

(defn insert-post-view!
  "Given a post and a bundle id, inserts a view event record 
  for the given post"
  [ds {:keys [id feed-id content-type-id creator-id] :as post} bundle-id]
  (let [bundle (bundles/bundle ds {:id bundle-id})
        event {:timestamp (util/get-utc-timestamp-string)
               :event "view"
               :feed-id feed-id
               :post-id id
               :content-type-id content-type-id
               :creator-id creator-id
               :bundle-id bundle-id
               :distributor-id (:user-id bundle)}
        event' (insert-event! ds {:data event
                                 :ret :*})]
    (insert-post-event-categories! ds event' post)))

(comment
  (require '[source.db.util :as db.util]
           '[honey.sql :as sql])

  (defonce ds (db.util/conn))

  (def maximums {:creators 100
                 :distributors 50
                 :feeds 150
                 :posts 6000
                 :bundles 75})

  (defn seed-event! [{:keys [creators distributors feeds posts bundles]}]
    (let [creator-id (inc (rand-int creators))
          distributor-id (inc (rand-int distributors))
          feed-id (inc (rand-int feeds))
          post-id (inc (rand-int posts))
          bundle-id (inc (rand-int bundles))
          event-type (rand-int 3)
          category-ids [(inc (rand-int 50)) (inc (rand-int 50))]

          new-event (hon/insert! ds {:tname :events
                                     :ret :1
                                     :data {:timestamp (util/get-utc-timestamp-string)
                                            :event (cond
                                                     (= event-type 0) "impression"
                                                     (= event-type 1) "click"
                                                     (= event-type 2) "view")
                                            :feed-id feed-id
                                            :post-id (when (> (rand-int 10) 3) post-id)
                                            :content-type-id (inc (rand-int 3))
                                            :creator-id creator-id
                                            :bundle-id bundle-id
                                            :distributor-id distributor-id}})

          event-categories (mapv (fn [x] {:event-id (:id new-event)
                                          :category-id x}) category-ids)]

      (hon/insert! ds {:tname :event-categories
                       :data event-categories})))

  (defn seed-events! [num-records]
    (dotimes [_ num-records]
      (seed-event! maximums)))

  (time (metric-query ds {:min-date "2025-11-25 15:00:00"
                          :feed-id "1"}))

  (time (statistics-query ds {:content-type 1
                              :creator-id 1
                              :bundle-id 1
                              :ret :1}))

  (time (hon/find ds {:tname :events
                      :where [:< :id 500]
                      :ret :*}))

  (time (hon/update! ds {:tname :events
                         :where [:between :id 5000000 5500000]
                         :data {:timestamp (str "2025-11-22" " 13:00:00")}
                         :ret :*}))

  (time (hon/execute! ds (- (hsql/select [[:date :timestamp] :day]
                                         [(hsql/filter :%count.* (hsql/where := :event "impression")) :impressions]
                                         [(hsql/filter :%count.* (hsql/where := :event "click")) :clicks]
                                         [(hsql/filter :%count.* (hsql/where := :event "view")) :views])
                            (hsql/from :events)
                            (hsql/where [:between :timestamp "2025-11-17 00:00:00" "2025-11-24 23:59:59"])
                            (hsql/group-by :day)
                            (hsql/order-by :day)) {:ret :*}))

  (time (interval-statistics-query ds :daily "2025-11-17" "2025-11-24" {:feed-id 4}))

  (time (hon/execute! ds (-> (hsql/select [[:strftime "%W" :timestamp] :week]
                                          [(hsql/filter :%count.* (hsql/where := :event "impression")) :impressions]
                                          [(hsql/filter :%count.* (hsql/where := :event "click")) :clicks]
                                          [(hsql/filter :%count.* (hsql/where := :event "view")) :views])
                             (hsql/from :events)
                             (hsql/where [:between :timestamp "2025-11-01 00:00:00" "2025-11-30 23:59:00"])
                             (hsql/group-by :week)
                             (hsql/order-by :week))
                      {:ret :*}))

  (time (interval-statistics-query ds :weekly "2025-11-01" "2025-11-30" {:feed-id 4}))
  (time (interval-statistics-query ds :monthly "2025-10-01" "2025-12-01" {:feed-id 4}))
  (time (interval-statistics-query ds :yearly "2024-01-01" "2026-01-01" {:feed-id 4}))

  (time (weekly-growth-averages ds "2025-11-01" "2025-11-30" {:feed-id 4}))

  (time (average-engagement ds "2025-11-24 00:00:00" "2025-11-24 23:59:59" {:feed-id 4}))

  (time (click-through-rate ds {:min-date "2025-11-24 00:00:00"
                                :max-date "2025-11-24 23:59:59"
                                :feed-id 4}))

  (time
   (hon/execute!
    ds
    {:select [[[:count :*] :total]]
     :from [:events]}
    {:ret :*}))

  (time (hon/find ds {:tname :event-categories
                      :ret :*}))

  (time (seed-event! maximums))

  (time (seed-events! 3000000))

  (time (sql/format (hsql/select [(hsql/filter :%count.* (hsql/where := :event "impression")) :impressions]
                                 [(hsql/filter :%count.* (hsql/where := :event "click")) :clicks]
                                 [(hsql/filter :%count.* (hsql/where := :event "view")) :views])))

  (time (insert-event! ds {:data {:timestamp (util/get-utc-timestamp-string)
                                 :event "impression"
                                 :feed-id 1
                                 :content-type-id 1
                                 :creator-id 1
                                 :bundle-id 1
                                 :distributor-id 1}}))

  ())

