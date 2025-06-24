(ns source.routes.interface
  (:require [source.routes.users :as users]
            [source.routes.user :as user]
            [source.routes.login :as login]
            [source.routes.register :as register]))

(defn users [request]
  (users/handler request))

(defn user [request]
  (users/handler request))

(defn login [request]
  (login/handler request))

(defn registe [request]
  (register/handler request))
