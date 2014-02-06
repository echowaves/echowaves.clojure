(ns echowaves.routes.wave
  (:require [compojure.core :refer [defroutes GET]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]
            [noir.util.route :refer [restricted]]))

(defn display-wave []
  (let [wave_name (session/get :wave)]
    (layout/render "wave.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_name
                  :images       (db/images-by-wave-blended wave_name)})))


(defn display-wave-json []
  (let [wave_name (session/get :wave)] 
    (noir.response/json (db/images-by-wave-blended wave_name))))


(defroutes wave-routes
  (GET "/wave" []
       (restricted(display-wave)))
  (GET "/wave.json" []
       (restricted(display-wave-json))))


