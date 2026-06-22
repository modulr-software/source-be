(ns source.migrations.027-bot-post-events
  (:require [source.db.master]
            [pg.core :as pg]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (pg/with-transaction [ds-master ds-master]
      (pg/execute
       ds-master
       "ALTER TABLE events 
        DROP CONSTRAINT IF EXISTS events_event_check")
      (pg/execute
       ds-master
       "ALTER TABLE events 
        ADD CONSTRAINT events_event_check 
        CHECK (event IN ('impression', 'click', 'view', 'bot_post'))"))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (pg/with-transaction [ds-master ds-master]
      (pg/execute
       ds-master
       "ALTER TABLE events 
        DROP CONSTRAINT IF EXISTS events_event_check")
      (pg/execute
       ds-master
       "ALTER TABLE events 
        ADD CONSTRAINT events_event_check 
        CHECK (event IN ('impression', 'click', 'view'))"))))

(comment
  (require '[source.db.util :as db.util]
           '[source.db.honey :as hon]
           '[honey.sql.helpers :as hsql])

  (hon/execute!
   (db.util/conn)
   (-> (hsql/select :constraint_name)
       (hsql/from :information_schema.table_constraints)
       (hsql/where [:and
                    [:= :table_name "events"]
                    [:= :constraint_type "CHECK"]]))
   {:ret :*})

  ())
