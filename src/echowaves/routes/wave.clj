(ns echowaves.routes.wave
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]))

(defn display-wave [wave_name]
  (layout/render "wave.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_name
                  :pictures     (db/images-by-wave wave_name)}))

(defroutes wave-routes
  (GET "/wave/:wave_name" [wave_name]
       (display-wave wave_name)))
