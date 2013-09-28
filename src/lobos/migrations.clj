(ns lobos.migrations
  (:refer-clojure 
   :exclude [alter drop bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]] core schema config)))


(defmigration add-waves-table
  (up []
      (create (table :waves
                     (integer :id :auto-inc :primary-key :unique)
                     (varchar :name 100 :unique)
                     (varchar :pass 100)
                     (timestamp :created_on (default (now)))))
      (create (index :waves  [:created_on])))
  (down [] (drop (table :waves))))


(defmigration add-images-table
  (up []
      (create (table :images
                     (integer :id :auto-inc :primary-key :unique)
                     (integer :wave_id :not-null)
                     (varchar :name 100 :not-null)
                     (timestamp :created_on :not-null (default (now)))))
      (create (index :images [:wave_id]))
      (create (index :images [:created_on])))
  (down [] (drop (table :images))))

