# rsql-utils

This library provides a type-safe query builder and various other utilities for working with [RSQL](https://github.com/nstdio/rsql-parser) style queries in Kotlin and Java. RSQL can be used as a generic query language for expressing filters on top of your data model and its URI-friendly syntax makes it an ideal candidate for exposing filter functionality in RESTful APIS.

For example, you can query your resource like this: `/movies?query=name=="Kill Bill";year=gt=2003` or `/movies?query=director.lastName==Nolan and year>=2000`. You can then use `rsql-utils` to parse the `query` query-parameter into a type-safe object model which can be integrated with your application. Out-of-the-box, the library comes with support for converting the query object model into:

* **RSQL expressions** (String): build queries using a type-safe builder pattern and then convert these to RSQL expression for use with other RSQL compatible software.
* **Java predicates**: build queries using a type-safe builder pattern and then convert these into Java predicates to perform in-memory tests on your data model (e.g. filtering through a collection).

By implementing a [NodeVisitor](src/main/kotlin/com/github/idlabdiscover/rsqlutils/model/NodeVisitor.kt), additional conversions can be added in a straightforward manner.

The library took inspiration from Paul Rutledge [Q-Builder library](https://github.com/RutledgePaulV/q-builders) and was designed as a framework for consolidating various query/filter models into a generic solution for reuse across projects.

## Basic usage

### From Kotlin
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

**Note**: adding a companion object and extending from `BuilderCompanion` allows us to supply your query class with a `create()` function that returns a dynamic proxy for your interface that implements all the property functions.

You can now start building RSQL style queries in a type-safe manner:

```kotlin
val q = PersonQuery.create().firstName().eq("Jane").and().lastName().eq("Doe")
```

The resulting `PersonQuery` instance implements `toString()` which can be used to serialize the query into an RSQL expression:

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

The resulting queries implement `equals()` and `hashCode()`, hence `q` and `parsedQ` being equal in the above example.

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

### From Java
The library is fully functional when calling from Java and basic usage is similar to the Kotlin examples above. However, Java does not have the Companion object language construct, so an instance of your query builder has to be constructed in another way.

The `PersonQuery` example can be defined in Java as follows:

```java
public interface PersonQuery extends Builder<PersonQuery> {

    LongProperty<PersonQuery> id();
    StringProperty<PersonQuery> firstName();
    StringProperty<PersonQuery> lastName();
    ShortProperty<PersonQuery> age();

}
```

Queries can then be constructed and parsed as follows:

```java
var q = queryBuilder(PersonQuery.class).create().firstName().eq("Jane").and().lastName().eq("Doe");
var rsql = q.toString();

val parsedQ = queryBuilder(PersonQuery.class).parse(rsql);
System.out.println(q.equals(parsedQ));

// Prints: true
```

The `queryBuilder` method is a static method that can be imported by adding the following import statement:

```java
import static com.github.idlabdiscover.rsqlutils.builder.BuilderKt.*;
```

## Advanced topics

### Using composed properties
Sometimes data modeling classes contain nested data structures. For example, say the `PersonRecord` has an address:

```kotlin
data class PersonRecord(val id: Long, val firstName: String, val lastName: String, val age: Short, val address: PersonAddress? = null)

data class PersonAddress(
    val street: String,
    val houseNumber: Int,
    val city: String,
    val postalCode: Int,
    val country: String
)
```

By implementing the `ComposedProperty` interface, the builder can support queries targeting a nested field. For example:

```kotlin
interface PersonQuery : Builder<PersonQuery> {
    companion object : BuilderCompanion<PersonQuery>(PersonQuery::class)

    fun id(): LongProperty<PersonQuery>
    fun firstName(): StringProperty<PersonQuery>
    fun lastName(): StringProperty<PersonQuery>
    fun age(): ShortProperty<PersonQuery>
    fun address(): AddressProperty
}

interface AddressProperty : ComposedProperty {
    fun street(): StringProperty<PersonQuery>
    fun houseNumber(): IntegerProperty<PersonQuery>
    fun city(): StringProperty<PersonQuery>
    fun postalCode(): IntegerProperty<PersonQuery>
    fun country(): StringProperty<PersonQuery>
}
```

Usage:

```kotlin
val q = PersonQuery.create().address().city().eq("Athens").and().address().country().eq("Greece")
println(q)

// Prints: address.city==Athens;address.country==Greece
```

### Implementing additional property types
In case the basic set of supported properties and composed property types are not sufficient, the library allows specifying additional custom property types.

**Important**: you must define a constructor that takes an instance of `PropertyHelper` as an argument. This helper object is supplied by the underlying builder proxy and facilitates implementing the properties.

In the next example, we implement a custom property for generating queries for URI properties:

```kotlin
class URIProperty<T : Builder<T>>(private val helper: PropertyHelper<T, URI>):
    EquitableProperty<T, URI> by helper, ListableProperty<T, URI> by helper
```

This example uses the [Kotlin delegate language construct](https://kotlinlang.org/docs/delegation.html) to delegate the implementation of the Property interfaces to the helper instance (allowing for a concise implementation). In Java, you will have to manually forward the method implementations.

Usage:

```kotlin
import java.net.URI

interface PersonQuery : Builder<PersonQuery> {
    companion object : BuilderCompanion<PersonQuery>(PersonQuery::class)
    
    /*
     Omitted the properties we've defined above.
     */
    fun homePage(): URIProperty<PersonQuery>
}

fun main() {
    val uri = URI.create("https://janedoe.example.org")
    val q = PersonQuery.create().homePage().eq(uri)
    println(q)
    
    // Prints: homePage==https://janedoe.example.org
    
    val parsedQ = PersonQuery.parse(q.toString()) // => throws an exception
}
```

Notice that the last statement (which parses the RSQL expression back into PersonQuery) throws an Exception. The reason for this, is that the parser does not know how to deserialize `https://janedoe.example.org` into a `URI` instance. Fortunately, additional property serializers/deserializers (SerDes) can easily be added (see next section).

### Overriding property serialization/deserialization
To completely support additional property types, you must implement the interface `PropertyValueSerDes` and add a mapping for a specific Property type when instantiating the Builder(Companion). E.g. say we want to be able to parse Person queries with URI properties, then we can implement the following class:

```kotlin
class URIPropertyValueSerDes : PropertyValueSerDes<URI> {
    override fun serialize(value: URI): String {
        return value.toASCIIString()
    }

    override fun deserialize(representation: String): URI {
        return URI.create(representation)
    }

}
```

And register this SerDes by modifying the Query definition:

```kotlin
interface PersonQuery : Builder<PersonQuery> {
    companion object : BuilderCompanion<PersonQuery>(PersonQuery::class, mapOf(URIProperty::class.java to URIPropertyValueSerDes))
    
    fun homePage(): URIProperty<PersonQuery>
}
```

Now parsing will succeed:

```kotlin
val uri = URI.create("https://janedoe.example.org")
val q = PersonQuery.create().homePage().eq(uri)

val parsedQ = PersonQuery.parse(q.toString())
println(q == parsedQ)

// Prints: true
```

### Using with Jackson JSON library
At IDLab, we often embed queries and filters in our application's data model, e.g. for modeling a scope for user permissions, views on top of resource collections, etc. As we often use [Jackson](https://github.com/FasterXML/jackson) to serialize/deserialize the data model to and from JSON, we've decided to streamline setting up Jackson support for the query builder types.

Say we have a data class defining a View on Person records:

```kotlin
data class PersonView(val viewId: String, val filter: PersonQuery)
```

You can then configure your Jackson `ObjectMapper` with support for `PersonQuery` by executing:

```kotlin
// Adding KotlinModule as well, allowing the mapper to process Kotlin data classes.
val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build(), TestQuery.generateJacksonModule())
```

The `PersonQuery` will now be serialized to JSON as a single String:

```kotlin
val json = mapper.writeValueAsString(PersonView("test", PersonQuery.create().age().gt(20)))
println(json)

// Prints: {"viewId":"test","filter":"age=gt=20"}
```
