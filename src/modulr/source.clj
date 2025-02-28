(ns modulr.source
  (:require [modulr.server :as server]))

(server/start-server)
(server/stop-server)
(server/running?)

(defn -main [& _]
  (println "Hello, Source!")
  )

(-main)