(ns picture-gallery.models.schema  
 (:use [lobos.core :only (defcommand migrate)])
  (:require [noir.io :as io]
            [lobos.migration :as lm]
            [environ.core :refer [env]]))

(def db-spec
  {:subprotocol "postgresql"
   :subname (env :pg-db-url) 
   :user (env :pg-db-user)
   :password (env :pg-db-pass)})


(defcommand pending-migrations []
  (lm/pending-migrations db-spec sname))

(defn actualized?
  "checks if there are no pending migrations"
  []
  (empty? (pending-migrations)))

(def actualize migrate)


