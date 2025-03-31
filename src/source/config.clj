(ns source.config
  (:require [aero.core :as aero]))

(def config
  (aero/read-config "config.edn"))
