(ns source.routes.interface
  (:require [source.routes.reitit :as reitit]))

(defn create-app []
  (reitit/create-app))

