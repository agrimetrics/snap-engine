CREATE TABLE versions(
	id SMALLINT PRIMARY KEY NOT NULL
)
;

CREATE TABLE sensor_types(
	id SMALLINT NOT NULL PRIMARY KEY,
	name VARCHAR(128) UNIQUE NOT NULL
)
;

CREATE TABLE pixel_types(
	id SMALLINT NOT NULL PRIMARY KEY,
	name VARCHAR(128) UNIQUE NOT NULL
)
;

CREATE TABLE data_format_types(
	id SMALLINT NOT NULL PRIMARY KEY,
	name VARCHAR(128) UNIQUE NOT NULL
)
;

CREATE TABLE local_repositories(
	id SMALLINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	folder_path VARCHAR(1024) UNIQUE NOT NULL
)
;

CREATE TABLE products(
	id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(256) NOT NULL,
	type VARCHAR(256) NOT NULL,
	local_repository_id SMALLINT NOT NULL,
	local_path VARCHAR(1024) NOT NULL,
	entry_point VARCHAR(256),
	size_in_bytes BIGINT NOT NULL,
	acquisition_date TIMESTAMP NOT NULL,
	last_modified_date TIMESTAMP NOT NULL,
    geometry GEOMETRY NOT NULL,
	data_format_type_id SMALLINT,
	pixel_type_id SMALLINT,
	sensor_type_id SMALLINT,
    FOREIGN KEY (local_repository_id) REFERENCES local_repositories(id),
    FOREIGN KEY (data_format_type_id) REFERENCES data_format_types(id),
    FOREIGN KEY (pixel_type_id) REFERENCES pixel_types(id),
    FOREIGN KEY (sensor_type_id) REFERENCES sensor_types(id)
)
;

CREATE TABLE product_remote_attributes(
	id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
	product_id INTEGER NOT NULL,
	name VARCHAR(256) NOT NULL,
	value VARCHAR(2048) NOT NULL,
	UNIQUE (product_id, name),
	FOREIGN KEY (product_id) REFERENCES products(id)
)
;

INSERT INTO sensor_types (id, name) VALUES (1, 'Optical')
;
INSERT INTO sensor_types (id, name) VALUES (2, 'Radar')
;
INSERT INTO sensor_types (id, name) VALUES (3, 'Altimetric')
;
INSERT INTO sensor_types (id, name) VALUES (4, 'Atmospheric')
;
INSERT INTO sensor_types (id, name) VALUES (5, 'Unknown')
;

INSERT INTO pixel_types (id, name) VALUES (1, 'Unsigned byte')
;
INSERT INTO pixel_types (id, name) VALUES (2, 'Signed byte')
;
INSERT INTO pixel_types (id, name) VALUES (3, 'Unsigned short')
;
INSERT INTO pixel_types (id, name) VALUES (4, 'Signed short')
;
INSERT INTO pixel_types (id, name) VALUES (5, 'Unsigned integer')
;
INSERT INTO pixel_types (id, name) VALUES (6, 'Signed integer')
;
INSERT INTO pixel_types (id, name) VALUES (7, 'Float')
;
INSERT INTO pixel_types (id, name) VALUES (8, 'Double')
;

INSERT INTO data_format_types (id, name) VALUES (1, 'Raster')
;
INSERT INTO data_format_types (id, name) VALUES (2, 'Vector')
;
INSERT INTO data_format_types (id, name) VALUES (3, 'Unknown')
;
