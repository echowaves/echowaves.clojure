(ns lobos.config
  (:use lobos.connectivity)
  (:require [echowaves.models.schema :as schema]))

(open-global schema/db-spec)
