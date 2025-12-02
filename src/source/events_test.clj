(ns source.events-test
  (:require [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [source.util :as util]
            [honey.sql :as sql]))

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

(defn insert-event [ds {:keys [data ret] :as opts}]
  (->> {:tname :events
        :data data
        :ret ret}
       (merge opts)
       (hon/insert! ds)))

