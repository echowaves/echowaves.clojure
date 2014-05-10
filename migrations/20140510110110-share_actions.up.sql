CREATE TABLE share_actions (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waves_id INT NOT NULL,
    images_id INT NOT NULL,
    token CHAR(20) NOT NULL,
    created_on TIMESTAMP DEFAULT NOW()
);

CREATE INDEX share_actions_index_created_on ON share_actions (created_on);
CREATE INDEX share_actions_index_wave_id ON share_actions (waves_id);
CREATE INDEX share_actions_index_image_id ON share_actions (images_id);
CREATE INDEX share_actions_index_token ON share_actions (token);
