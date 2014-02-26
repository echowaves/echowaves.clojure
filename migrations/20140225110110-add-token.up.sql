CREATE TABLE ios_tokens (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waves_id INT NOT NULL,
    token CHAR(64) NOT NULL,
    created_on TIMESTAMP DEFAULT NOW()
);

CREATE INDEX ios_tokens_index_created_on ON ios_tokens (created_on);
CREATE INDEX ios_tokens_index_wave_id ON ios_tokens (waves_id);
CREATE INDEX ios_tokens_index_token ON ios_tokens (token);

