(ns echowaves.routes.echowaves
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]))

(defn display-echowaves [wave_id]
  (layout/render "echowaves.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_id
                  :pictures     (db/images-by-wave wave_id)}))

(defroutes echowaves-routes
  (GET "/echowaves/:wave_id" [wave_id]
       (display-echowaves wave_id)))
