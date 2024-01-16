# rsql-utils

This library provides a type-safe query builder and various other utilities for working with [RSQL](https://github.com/nstdio/rsql-parser) style queries in Kotlin and Java. RSQL can be used as a generic query language for expressing filters on top of your data model and its URI-friendly syntax makes it an ideal candidate for exposing filter functionality in RESTful APIS.

For example, you can query your resource like this: `/movies?query=name=="Kill Bill";year=gt=2003` or `/movies?query=director.lastName==Nolan and year>=2000`. You can then use `rsql-utils` to parse the `query` query-parameter into a type-safe object model which can be integrated with your application. Out-of-the-box, the library comes with support for converting the query object model into:

* **RSQL expressions** (String): build queries using a type-safe builder pattern and then convert these to RSQL expression for use with other RSQL compatible software.
* **Java predicates**: build queries using a type-safe builder pattern and then convert these into Java predicates to perform in-memory tests on your data model (e.g. filtering through a collection).

By implementing a [NodeVisitor](src/main/kotlin/com/github/idlabdiscover/rsqlutils/model/NodeVisitor.kt), additional conversions can be added in a straightforward manner.

The library took inspiration from Paul Rutledge [Q-Builder library](https://github.com/RutledgePaulV/q-builders) and was designed as a framework for consolidating various query/filter models into a generic solution for reuse across projects.

## Basic usage
Start with defining a query model for one of your data classes. This is done by creating an interface that extends from [Builder](src/main/kotlin/com/github/idlabdiscover/rsqlutils/builder/Builder.kt). Say we have a data class `PersonRecord`, defined as:

```kotlin
data class PersonRecord(val id: Long, val firstName: String, val lastName: String, val age: Short)
```

A matching query model could then be written as:

```kotlin
interface PersonQuery : Builder<PersonQuery> {
    companion object : BuilderCompanion<PersonQuery>(PersonQuery::class)

    fun id(): LongProperty<PersonQuery>
    fun firstName(): StringProperty<PersonQuery>
    fun lastName(): StringProperty<PersonQuery>
    fun age(): ShortProperty<PersonQuery>
}
```

**Note**: adding a companion object and extending from `BuilderCompanion` is required. This allows us to supply your query class with a `create()` function that returns a dynamic proxy for your interface that implements all the property functions.

You can now start building RSQL style queries in a type-safe manner:

```kotlin
val q = PersonQuery.create().firstName().eq("Jane").and().lastName().eq("Doe")
```

The resulting `Condition<PersonQuery>` implements `toString()` which can be used to serialize the query into an RSQL expression:

```kotlin
val q = PersonQuery.create().firstName().eq("Jane").and().lastName().eq("Doe")
println(q)

// Prints: firstName==Jane;lastName==Doe
```

You can parse a valid RSQL expression using the `parse()` function generated for your `PersonQuery` interface:

```kotlin
val q = PersonQuery.create().firstName().eq("Jane").and().lastName().eq("Doe")
val rsql = q.toString()
val parsedQ = PersonQuery.parse(rsql)
println(q == parsedQ)

// Prints: true
```

The resulting conditions implement `equals()` and `hashCode()`, hence `q` and `parsedQ` being equal in the above example.

Use the `asPredicate()` function to convert the query into a Java predicate, so it can be used to perform in-memory filtering/condition checks on your data model. For example:

```kotlin
// Say we have a collection of PersonRecords:
val person: Collection<PersonRecord> = getPersons()

// Using the following query, you can then perform in-memory filtering of this collection:
val predicate = PersonQuery.create().age().gt(25).asPredicate<PersonRecord>()
person.filter(predicate::test)
```

`rsql-utils` relies on the visitor design pattern for implementing conversions from the query model into different targets. For the built-in conversions, easy to remember functions have been added, but internally these also rely on a specific Visitor implementation. For example:

```kotlin
val q = PersonQuery.create().firstName().eq("Jane").and().lastName().eq("Doe")
// Writing ...
q.toString()
// ... is the same as writing:
q.visitUsing(RSQLVisitor(PersonQuery.builderConfig))

// Or writing ...
q.asPredicate<PersonRecord>()
// ... is the same as writing:
q.visitUsing(PredicateVisitor())
```

The visitor pattern makes it easy to extend `rsql-utils` with custom query targets (e.g. MongoDB criteria, SQL, etc): you only have to provide an implementation of the `NodeVisitor` interface.

## Advanced topics

### Using composed properties

### Implementing additional property types

### Overriding property serialization/deserialization

### Using with Jackson JSON library
