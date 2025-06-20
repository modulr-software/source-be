(ns source.middleware.interface
  (:require [source.middleware.core :as mw]))

(defn apply-generic [app]
  (mw/apply-generic app))

(defn apply-auth [app & {:keys [required-type]}]
  (mw/apply-auth app {:required-type required-type}))

