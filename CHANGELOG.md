# Changelog

## 1.0.0 (2024-01-23)


### Features

* added support for predicate mapping and JSON serialization ([e8581b0](https://github.com/idlab-discover/rsql-utils/commit/e8581b0d13f939b2fcf8d4a48059fb100b78d381))
* generating an RSQL-expression now possible without a dedicated SerDes (falls back to .toString). ([8a312dd](https://github.com/idlab-discover/rsql-utils/commit/8a312ddaabd2acc886a6010e891f4bb6aac911c3))
* introduced a top-level function to streamline usage from Java ([91a8d0a](https://github.com/idlab-discover/rsql-utils/commit/91a8d0ad1a1ec76cc49b5b153e5b44521b1dfbed))


### Bug Fixes

* can now support multiple query types as Jackson modules, by typing via effective query type instead of generic Condition&lt;T&gt;, this required some refactoring. ([56d4af6](https://github.com/idlab-discover/rsql-utils/commit/56d4af6fcb082cba1a31d1b5b58d25a5b7e62437))
