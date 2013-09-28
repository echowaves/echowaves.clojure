(ns picture-gallery.models.db
  (:require [clojure.java.jdbc :as sql]
            [korma.db :refer [defdb transaction]]
            [korma.core :refer :all]
            [picture-gallery.models.schema :as schema]))

(defdb db schema/db-spec)

;; (defdb korma-db db)

(defentity users)

(defentity images)

(defn create-user [user]
  (insert users (values user)))

(defn get-user [id]
  (first (select users
                 (where {:id id})
                 (limit 1))))
                 
(defn delete-user [id]
  (delete users (where {:id id})))  

(defn add-image [userid name]  
  (transaction
    (if (empty? (select images 
                        (where {:userid userid :name name})
                        (limit 1)))
      (insert images (values {:userid userid :name name}))
      (throw 
        (Exception. "you have already uploaded an image with the same name")))))
                           
(defn images-by-user [userid]
  (select images (where {:userid userid})))
                 
(defn delete-image [userid name]
  (delete images (where {:userid userid :name name}))) 

(defn get-gallery-previews []
  (exec-raw
    ["select *, row_number() over() as r_num from (select *, row_number() over (partition by userid) as row_number from images) as rows where row_number = 1 order by r_num desc limit 100" []] ;; show last 100 waves
     :results)) 
