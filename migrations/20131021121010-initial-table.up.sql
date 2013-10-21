-- mysqladmin -u root -p create echowaves
-- http://www.bigmarv.net/blog/?p=401

CREATE TABLE waves (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    pass VARCHAR(100) NOT NULL,
    created_on TIMESTAMP DEFAULT NOW()
);

CREATE INDEX waves_index_created_on ON waves (created_on);
CREATE TABLE images (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waves_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_on TIMESTAMP DEFAULT NOW()
);
CREATE INDEX images_index_created_on ON images (created_on);
CREATE INDEX images_index_waves_id ON images (waves_id);



