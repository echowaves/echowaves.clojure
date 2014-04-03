ALTER TABLE waves DROP active;
ALTER TABLE waves MODIFY pass varchar(100) not null;
