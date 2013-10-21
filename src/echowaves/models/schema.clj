(ns echowaves.models.schema  
  (:require [noir.io :as io]
            [environ.core :refer [env]]))

(def db-spec
  {:subprotocol "mysql"
   :subname (env :pg-db-url) 
   :user (env :pg-db-user)
   :password (env :pg-db-pass)})



