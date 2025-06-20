(ns source.middleware.interface
  (:require [source.middleware.core :as mw]))

(defn apply-generic [app]
  (mw/apply-generic app))

(defn apply-auth [app]
  (mw/apply-auth app))

(defn apply-admin-auth [app]
  (mw/apply-admin-auth app))

