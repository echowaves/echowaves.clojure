DROP index waves_index_name on waves;
ALTER TABLE waves DROP parent_wave_id;
CREATE UNIQUE INDEX waves_index_name ON waves (name);
