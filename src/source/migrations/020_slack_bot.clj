(ns source.migrations.020-slack-bot
  (:require [source.db.tables :as tables]
            [source.db.master]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-table! ds-master :source.db.master :integration-channels)))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-table! ds-master :integration-channels)))
