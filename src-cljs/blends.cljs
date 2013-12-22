(ns blends
  (:require [goog.dom :as dom]
            [domina :refer [by-id nodes append!]]
            [domina.events :refer [listen!]]
            [domina.css :refer [sel]]
            [ajax.core :refer [POST]]))


(defn ^:export init []
  (listen! (by-id "delete") :click deleteImages))
