(ns source.services.outgoing-posts
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn outgoing-post [ds {:keys [id where] :as opts}]
  (->> {:tname :outgoing-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn upsert-outgoing-posts! [ds {:keys [data]}]
  (hon/execute!
   ds
   (-> (hsql/insert-into :outgoing-posts)
       (hsql/values data)
       (assoc :on-conflict [:post-id])
       (assoc :do-update-set {:feed-id          :excluded.feed-id
                              :title            :excluded.title
                              :info             :excluded.info
                              :url              :excluded.url
                              :stream-url       :excluded.stream-url
                              :creator-id       :excluded.creator-id
                              :season           :excluded.season
                              :episode          :excluded.episode
                              :content-type-id  :excluded.content-type-id
                              :posted-at        :excluded.posted-at}))))
