(ns lobos.config
  (:use lobos.connectivity)
  (:require [picture-gallery.models.schema :as schema]))

(open-global schema/db-spec)
