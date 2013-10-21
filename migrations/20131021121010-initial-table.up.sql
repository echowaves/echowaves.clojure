-- mysqladmin -u root -p create echowaves
-- http://www.bigmarv.net/blog/?p=401

CREATE TABLE images (
    id integer NOT NULL,
    waves_id integer NOT NULL,
    name character varying(100) NOT NULL,
    created_on timestamp without time zone DEFAULT now() NOT NULL
);


CREATE TABLE waves (
    id integer NOT NULL,
    name character varying(100),
    pass character varying(100),
    created_on timestamp without time zone DEFAULT now()
);


CREATE INDEX images_index_created_on ON images USING btree (created_on);

CREATE INDEX images_index_waves_id ON images USING btree (waves_id);

CREATE INDEX waves_index_created_on ON waves USING btree (created_on);

