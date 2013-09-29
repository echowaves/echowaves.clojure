(ns echowaves.routes.echowaves
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]))

(defn display-echowaves [userid]
  (layout/render "echowaves.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   userid
                  :pictures     (db/images-by-user userid)}))

(defroutes echowaves-routes
  (GET "/echowaves/:userid" [userid]
       (display-echowaves userid)))
