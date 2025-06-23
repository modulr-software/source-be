(ns source.routes.interface
  (:require [source.routes.users :as users]
            [source.routes.user :as user]))

(defn users [request]
  (users/handler request))

(defn user [request]
  (users/handler request))
