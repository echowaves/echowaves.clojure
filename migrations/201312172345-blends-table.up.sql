CREATE TABLE blends (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    wave_id1 INT NOT NULL,
    wave_id2 INT NOT NULL,
    created_on TIMESTAMP DEFAULT NOW(),
    confirmed_on TIMESTAMP NULL,
    UNIQUE KEY blends_index_waves_ids (wave_id1, wave_id2)
);

CREATE INDEX blends_index_created_on ON blends (created_on);
CREATE INDEX blends_index_confirmed_on ON blends (confirmed_on);
CREATE INDEX blends_index_followers_id ON blends (wave_id1);
CREATE INDEX blends_index_followings_id_id ON blends (wave_id2);

CREATE INDEX images_index_name ON images (name);
