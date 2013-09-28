(ns echowaves.routes.home
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]))

(defn home [] 
  (layout/render "home.html"
                 {:thumb-prefix thumb-prefix                  
                  :galleries    (db/get-gallery-previews)}))

(defroutes home-routes
  (GET "/" [] (home)))
