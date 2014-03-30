ALTER TABLE waves ADD parent_wave_id INT after name;
DROP index waves_index_name on waves;
CREATE UNIQUE INDEX waves_index_name ON waves (name, parent_wave_id);
