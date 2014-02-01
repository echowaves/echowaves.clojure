ALTER TABLE waves ADD token CHAR(255);
ALTER TABLE waves ADD token_expires_on TIMESTAMP;

CREATE INDEX waves_index_token ON waves (token);
