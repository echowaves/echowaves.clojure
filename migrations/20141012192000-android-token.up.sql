CREATE TABLE android_tokens (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waves_id INT NOT NULL,
    token LONGBLOB NOT NULL,
    created_on TIMESTAMP DEFAULT NOW()
) ENGINE=MyISAM;

CREATE INDEX android_tokens_index_created_on ON android_tokens (created_on);
CREATE INDEX android_tokens_index_wave_id ON android_tokens (waves_id);
CREATE INDEX android_tokens_index_token ON android_tokens (token(1000));
