CREATE TABLE test_table_1 (
  id INT NOT NULL,
  enum_field VARCHAR(50),
  big_field VARCHAR(1000),
  read_only INT,
  is_deleted TINYINT,
  double_field DOUBLE,
  char_field CHAR(10),
  date_field DATE,
  time_field TIME,
  timestamp_field TIMESTAMP,
  decimal_field_1 DECIMAL(5),
  decimal_field_2 DECIMAL(10,2),
  numeric_field NUMERIC,
  boolean_field BOOLEAN,
  smallint_field SMALLINT,
  bigint_field BIGINT,
  real_field REAL,
  binary_field BINARY,
  varbinary_field VARBINARY(50),
  other_field OTHER,
  PRIMARY KEY (id)
);

CREATE TABLE test_table_2 (
  id INT NOT NULL,
  enum_field_2 VARCHAR(50),
  big_field_2 VARCHAR(1000),
  read_only INT,
  test_array INT ARRAY DEFAULT ARRAY[],
  is_deleted TINYINT,
  PRIMARY KEY (id)
);

CREATE TABLE test_table_3 (
  id INT NOT NULL,
  enum_field_2 VARCHAR(50),
  big_field_2 VARCHAR(1000),
  read_only INT,
  is_deleted TINYINT,
  PRIMARY KEY (id)
);
