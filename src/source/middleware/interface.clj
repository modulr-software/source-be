(ns source.middleware.interface
  (:require [source.middleware.core :as mw]))

(defn apply-generic [app & {:keys [ds]}]
  (mw/apply-generic app :ds ds))

(defn apply-auth
  "accepts required-type as an optional parameter to authorize the route only for the specified user type"
  [app & {:keys [required-type]}]
  (mw/apply-auth app {:required-type required-type}))

