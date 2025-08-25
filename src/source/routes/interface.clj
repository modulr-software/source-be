(ns source.routes.interface
  (:require [source.routes.reitit :as reitit]))

(defn create-app [{:keys [ds store js] :as opts}]
  (reitit/create-app opts))

