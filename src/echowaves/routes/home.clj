(ns echowaves.routes.home
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [echowaves.routes.wave :as wave]
            [noir.session :as session]))

(defn home [] 
  (layout/render "home.html"
                 {:thumb-prefix thumb-prefix                  
                  :echowaves (db/get-echowaves-previews)}))

(defroutes home-routes
  (GET "/" []
       (if (session/get :wave)
         (wave/display-wave (session/get :wave))
         (home))))
