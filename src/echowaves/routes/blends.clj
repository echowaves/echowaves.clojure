(ns echowaves.routes.blends
  (:require [compojure.core :refer [defroutes GET POST]]
            [echowaves.views.layout :as layout]
            [noir.io :refer [resource-path]]
            [noir.session :as session]
            [noir.response :as resp]            
            [clojure.java.io :as io]
            [echowaves.models.db :as db]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            [noir.util.route :refer [restricted]]            )
  )

(defn handle-blended-with []
  (let [wave (db/get-wave (session/get :wave))]
    (noir.response/json (db/blended-with (:id wave)))))


(defroutes blends-routes
  (GET "/blended-with" []
       (restricted (handle-blended-with)) ))
