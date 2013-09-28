(ns picture-gallery.util
  (:require [noir.io :refer [resource-path]]
            [noir.session :as session])
  (:import java.io.File))

(def thumb-prefix "thumb_")

(defn gallery-path []
  (str (resource-path) "img" File/separator (session/get :user) File/separator))
