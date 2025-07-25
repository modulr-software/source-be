(ns source.source
  (:require [source.server :as server]
            [source.hooks :as hooks])
  (:gen-class))

(defn -main [& _]
  (server/start-server)
  (hooks/add-shutdown-hook server/stop-server))
