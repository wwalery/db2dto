# DB2DTO Copyright (c) 2020 - 2021
Author: Walery Wysotsky <dev@wysotsky.info>

## [1.5.0] - 2021-03-08
### Added
- Detect simple types for additional fields and use it for non-nullable columns

## [1.4.1] - 2021-01-10
### Fixed
- Lost version number

## [1.4.0] - 2021-01-10
### Added
- set specific method names instead of based on column name
- templates: add method to class for return current values for all fields

## [1.3.0] - 2020-08-05
### Added
- use simple java types when possible, if column non-nullable
- method get...NonNull() generated only for nullable columns

### Fixed
- return empty ArrayList (depends on settings) as default value for array columns

## [1.2.0] - 2020-08-02
### Added
- Ability to return default value for field to column class and plugin
- generate method get...NonNull() for DB column based on default (non DB-default) column value

