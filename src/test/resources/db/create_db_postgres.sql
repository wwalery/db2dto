CREATE TABLE test_table_1 (
  id INT NOT NULL,
  enum_field VARCHAR(50),
  big_field VARCHAR(1000),
  read_only INT,
  is_deleted SMALLINT DEFAULT 0,
  double_field DOUBLE PRECISION,
  char_field CHAR(10),
  date_field DATE,
  time_field TIME,
  timestamp_field TIMESTAMP,
  decimal_field_1 DECIMAL(5,0),
  decimal_field_2 DECIMAL(10,2),
  numeric_field NUMERIC,
  boolean_field BOOLEAN DEFAULT true,
  smallint_field SMALLINT,
  bigint_field BIGINT,
  real_field REAL,
  binary_field BYTEA,
  varbinary_field BYTEA,
  other_field JSONB,
  uuid_field UUID,
  PRIMARY KEY (id)
);

CREATE TABLE test_table_2 (
  id INT NOT NULL,
  enum_field_2 VARCHAR(50) DEFAULT 'test',
  big_field_2 VARCHAR(1000),
  read_only INT,
  test_array INT[] DEFAULT ARRAY[]::INT[],
  test_array2 VARCHAR(50)[] DEFAULT ARRAY[]::VARCHAR(50)[],
  test_object JSONB,
  is_deleted SMALLINT,
  PRIMARY KEY (id)
);

CREATE TABLE test_table_3 (
  id INT NOT NULL,
  enum_field_2 VARCHAR(50),
  big_field_2 VARCHAR(1000),
  read_only INT,
  is_deleted SMALLINT,
  PRIMARY KEY (id)
);


