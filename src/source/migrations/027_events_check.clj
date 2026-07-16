(ns source.migrations.027-events-check
  (:require [source.db.master]
            [pg.core :as pg]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (pg/execute
     ds-master
     "ALTER TABLE events 
      DROP CONSTRAINT IF EXISTS events_event_check")))

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
        CHECK (event IN ('impression', 'click', 'view', 'bot_post'))"))))
