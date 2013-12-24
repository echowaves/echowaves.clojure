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
(defn handle-requiested-blends []
  (let [wave (db/get-wave (session/get :wave))]
    (noir.response/json (db/requested-blends (:id wave)))))
(defn handle-unconfirmed-blends []
  (let [wave (db/get-wave (session/get :wave))]
    (noir.response/json (db/unconfirmed-blends (:id wave)))))
(defn handle-autocomplete-wave-name [term]
  (info "autocompleting: " term)
  (noir.response/json (db/autocomplete-wave-name term)))


(defroutes blends-routes
  (GET "/blended-with.json" []
       (restricted (handle-blended-with)) )
  (GET "/requested-blends.json" []
       (restricted (handle-requiested-blends)) )
  (GET "/unconfirmed-blends.json" []
       (restricted (handle-unconfirmed-blends)) )
  (GET "/autocomplete-wave-name.json" [term]
       (restricted (handle-autocomplete-wave-name term)) ))
