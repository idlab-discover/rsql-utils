package com.github.idlabdiscover.rsqlutils.builder

import com.github.idlabdiscover.rsqlutils.builder.utils.DemoEnum
import com.github.idlabdiscover.rsqlutils.builder.utils.ExampleQuery
import com.github.idlabdiscover.rsqlutils.builder.utils.ExampleQueryWithCustomSerDes
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery
import com.github.idlabdiscover.rsqlutils.model.EquitableProperty
import com.github.idlabdiscover.rsqlutils.model.ListableProperty
import com.github.idlabdiscover.rsqlutils.model.PropertyHelper
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

class BuilderTest {

    @Test
    fun testStringPropertyQuery() {
        testQuery(
            "stringProperty=gt=\"John Doe\"",
            ExampleQuery.create().stringProperty().lexicallyAfter("John Doe"),
            ExampleQuery
        )

        testQuery(
            "stringProperty!=\"test: \\\"hello\\\" and 'hello', also here is a slash: \\\\\"",
            ExampleQuery.create().stringProperty().ne("test: \"hello\" and 'hello', also here is a slash: \\"),
            ExampleQuery
        )
    }

    @Test
    fun testInOperator() {
        testQuery("intProperty=in=(4,5,6)", ExampleQuery.create().intProperty().valueIn(4, 5, 6), ExampleQuery)
        testQuery(
            "stringProperty=out=(test1,test2,test3)",
            ExampleQuery.create().stringProperty().valueNotIn("test1", "test2", "test3"),
            ExampleQuery
        )
    }

    @Test
    fun testComposedQuery() {
        testQuery(
            "stringProperty==test;doubleProperty=gt=20.0;booleanProperty==false",
            ExampleQuery.create().stringProperty().eq("test").and().doubleProperty().gt(20.0).and().booleanProperty()
                .isFalse(),
            ExampleQuery
        )
    }

    @Test
    fun testComposedQueryWithPrecedence() {
        testQuery(
            "(floatProperty=lt=1.0,floatProperty=gt=0.5);booleanProperty==true",
            ExampleQuery.create().and(
                ExampleQuery.create().floatProperty().lt(1.0).or().floatProperty().gt(0.5),
                ExampleQuery.create().booleanProperty().isTrue()
            ),
            ExampleQuery
        )
    }

    @Test
    fun testInstantQuery() {
        val instant = Instant.ofEpochMilli(1704063600000)
        testQuery(
            "instantProperty=lt=2024-01-01T01:00:00Z;instantProperty=gt=2023-12-31T23:00:00Z",
            ExampleQuery.create().instantProperty().before(instant.plus(2, ChronoUnit.HOURS)).and().instantProperty()
                .after(instant),
            ExampleQuery
        )
    }

    @Test
    fun testInstantQueryWithCustomSerDes() {
        val instant = Instant.ofEpochMilli(1704063600000)
        testQuery(
            "instantProperty=lt=1704070800000;instantProperty=gt=1704063600000",
            ExampleQueryWithCustomSerDes.create().instantProperty().before(instant.plus(2, ChronoUnit.HOURS)).and()
                .instantProperty()
                .after(instant),
            ExampleQueryWithCustomSerDes
        )
    }

    @Test
    fun testEnumQuery() {
        testQuery(
            "enumProperty==VALUE1",
            ExampleQueryWithCustomSerDes.create().enumProperty().eq(DemoEnum.VALUE1),
            ExampleQueryWithCustomSerDes
        )
    }

    @Test
    fun testComposedProperty() {
        testQuery(
            "address.city==Athens;address.country==Greece",
            TestQuery.create().address().city().eq("Athens").and().address().country().eq("Greece"),
            TestQuery
        )
    }

    @Test
    fun testNoOperands() {
        // Having no operands should result in an empty expression
        testQuery(
            "",
            ExampleQuery.create().and().and(ExampleQuery.create().or(), ExampleQuery.create().or()),
            ExampleQuery
        )
    }

    @Test
    fun testCustomProperty() {
        testQuery("homePage==https://janedoe.example.org", TestQuery.create().homePage().eq(URI.create("https://janedoe.example.org")), TestQuery)
    }
}

fun <T : Builder<T>> testQuery(expectedRSQL: String, q: T, companion: BuilderCompanion<T>) {
    // Test query builder and serialization
    val rsql = q.toString()
    logger.debug { "Constructed query: $rsql" }
    assertEquals(expectedRSQL, rsql)

    // Test query parsing (deserialization)
    val deserializedQ = companion.parse(rsql)
    assertEquals(q, deserializedQ)
}