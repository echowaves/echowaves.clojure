(ns echowaves.util
  (:require [noir.io :refer [resource-path]]
            [noir.session :as session])
  (:import java.io.File))

(def thumb-prefix "thumb_")

(defn session-wave-path []
  (str (resource-path) "img" File/separator (session/get :wave) File/separator))
