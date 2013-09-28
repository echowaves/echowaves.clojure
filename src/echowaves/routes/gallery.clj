(ns echowaves.routes.gallery
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]))

(defn display-gallery [userid]
  (layout/render "gallery.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   userid
                  :pictures     (db/images-by-user userid)}))

(defroutes gallery-routes
  (GET "/gallery/:userid" [userid]
       (display-gallery userid)))
