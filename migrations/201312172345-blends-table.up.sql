CREATE TABLE blends (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    followers_id INT NOT NULL,
    followings_id INT NOT NULL,
    created_on TIMESTAMP DEFAULT NOW(),
    confirmed_on TIMESTAMP NULL 
);

CREATE INDEX blends_index_created_on ON blends (created_on);
CREATE INDEX blends_index_confirmed_on ON blends (confirmed_on);
CREATE INDEX blends_index_followers_id ON blends (followers_id);
CREATE INDEX blends_index_followings_id_id ON blends (followings_id);

CREATE INDEX images_index_name ON images (name);
