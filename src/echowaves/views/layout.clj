(ns picture-gallery.views.layout
  (:require [noir.session :as session]
            [selmer.parser :as parser]
            [ring.util.response :refer [response]])
  (:import compojure.response.Renderable))

(def template-folder "picture_gallery/views/templates/")

(deftype RenderablePage [template params]
  Renderable
  (render [this request]
    (->> (assoc params
                :context (:context request)
                :user    (session/get :user))
         (parser/render-file (str template-folder template))
         response)))

(defn render [template & [params]]
  (RenderablePage. template params))
