# Changelog

## [1.3.0](https://github.com/idlab-discover/rsql-utils/compare/v1.2.0...v1.3.0) (2024-03-07)


### Features

* introduced new property type StringMapProperty (useful for modeling e.g. properties/labels) ([91538fb](https://github.com/idlab-discover/rsql-utils/commit/91538fbf3be91ea1225b1cdc90b71169b6233dcd))

## [1.2.0](https://github.com/idlab-discover/rsql-utils/compare/v1.1.0...v1.2.0) (2024-01-24)


### Features

* You can now specify the classloader to use when constructing or parsing queries ([760d5d4](https://github.com/idlab-discover/rsql-utils/commit/760d5d4ddc811ed7f794eff3f43cfe6fd879932d))

## [1.1.0](https://github.com/idlab-discover/rsql-utils/compare/v1.0.0...v1.1.0) (2024-01-24)


### Features

* The custom SerDes mechanism can now support entire class hierarchies of Properties with a single mapping. ([ef1c7e1](https://github.com/idlab-discover/rsql-utils/commit/ef1c7e1dd23f930189c4cf46419e782a1153f6e0))


### Bug Fixes

* Builder should ommit empty logical nodes when chaining using and()/or() ([84e5030](https://github.com/idlab-discover/rsql-utils/commit/84e50301c4aca23f3db17f32036764b5a2a3fb3d))
* Default operator when chaining without an explicit logical operator should be and(), not or() ([ddeac09](https://github.com/idlab-discover/rsql-utils/commit/ddeac0909b1b7986142f003ccb0bfd7cfbcb3cca))

## 1.0.0 (2024-01-23)


### Features

* added support for predicate mapping and JSON serialization ([e8581b0](https://github.com/idlab-discover/rsql-utils/commit/e8581b0d13f939b2fcf8d4a48059fb100b78d381))
* generating an RSQL-expression now possible without a dedicated SerDes (falls back to .toString). ([8a312dd](https://github.com/idlab-discover/rsql-utils/commit/8a312ddaabd2acc886a6010e891f4bb6aac911c3))
* introduced a top-level function to streamline usage from Java ([91a8d0a](https://github.com/idlab-discover/rsql-utils/commit/91a8d0ad1a1ec76cc49b5b153e5b44521b1dfbed))


### Bug Fixes

* can now support multiple query types as Jackson modules, by typing via effective query type instead of generic Condition&lt;T&gt;, this required some refactoring. ([56d4af6](https://github.com/idlab-discover/rsql-utils/commit/56d4af6fcb082cba1a31d1b5b58d25a5b7e62437))


### Miscellaneous Chores

* release 1.0.0 ([32a0e89](https://github.com/idlab-discover/rsql-utils/commit/32a0e895b299df46972015034f713b782941112e))
