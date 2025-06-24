(ns source.routes.interface
  (:require [source.routes.user :as user]
            [source.routes.users :as users]
            [source.routes.login :as login]
            [source.routes.register :as register]))

(defn users [request]
  (users/handler request))

(defn user [request]
  (user/get-handler request))

(defn update-user [request]
  (user/patch-handler request))

(defn login [request]
  (login/handler request))

(defn register [request]
  (register/handler request))
