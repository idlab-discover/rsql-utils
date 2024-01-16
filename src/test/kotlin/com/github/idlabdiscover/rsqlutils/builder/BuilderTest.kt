package com.github.idlabdiscover.rsqlutils.builder

import com.github.idlabdiscover.rsqlutils.builder.utils.DemoEnum
import com.github.idlabdiscover.rsqlutils.builder.utils.ExampleBuilder
import com.github.idlabdiscover.rsqlutils.builder.utils.ExampleBuilderWithCustomSerDes
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery
import com.github.idlabdiscover.rsqlutils.model.*
import com.github.idlabdiscover.rsqlutils.serdes.AbstractPropertyValueSerDes
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

class BuilderTest {

    @Test
    fun testStringPropertyQuery() {
        testQuery(
            "stringProperty=gt=\"John Doe\"",
            ExampleBuilder.create().stringProperty().lexicallyAfter("John Doe"),
            ExampleBuilder
        )

        testQuery(
            "stringProperty!=\"test: \\\"hello\\\" and 'hello', also here is a slash: \\\\\"",
            ExampleBuilder.create().stringProperty().ne("test: \"hello\" and 'hello', also here is a slash: \\"),
            ExampleBuilder
        )
    }

    @Test
    fun testInOperator() {
        testQuery("intProperty=in=(4,5,6)", ExampleBuilder.create().intProperty().valueIn(4, 5, 6), ExampleBuilder)
        testQuery(
            "stringProperty=out=(test1,test2,test3)",
            ExampleBuilder.create().stringProperty().valueNotIn("test1", "test2", "test3"),
            ExampleBuilder
        )
    }

    @Test
    fun testComposedQuery() {
        testQuery(
            "stringProperty==test;doubleProperty=gt=20.0;booleanProperty==false",
            ExampleBuilder.create().stringProperty().eq("test").and().doubleProperty().gt(20.0).and().booleanProperty()
                .isFalse(),
            ExampleBuilder
        )
    }

    @Test
    fun testComposedQueryWithPrecedence() {
        testQuery(
            "(floatProperty=lt=1.0,floatProperty=gt=0.5);booleanProperty==true",
            ExampleBuilder.create().and(
                ExampleBuilder.create().floatProperty().lt(1.0).or().floatProperty().gt(0.5),
                ExampleBuilder.create().booleanProperty().isTrue()
            ),
            ExampleBuilder
        )
    }

    @Test
    fun testInstantQuery() {
        val instant = Instant.ofEpochMilli(1704063600000)
        testQuery(
            "instantProperty=lt=2024-01-01T01:00:00Z;instantProperty=gt=2023-12-31T23:00:00Z",
            ExampleBuilder.create().instantProperty().before(instant.plus(2, ChronoUnit.HOURS)).and().instantProperty()
                .after(instant),
            ExampleBuilder
        )
    }

    @Test
    fun testInstantQueryWithCustomSerDes() {
        val instant = Instant.ofEpochMilli(1704063600000)
        testQuery(
            "instantProperty=lt=1704070800000;instantProperty=gt=1704063600000",
            ExampleBuilderWithCustomSerDes.create().instantProperty().before(instant.plus(2, ChronoUnit.HOURS)).and()
                .instantProperty()
                .after(instant),
            ExampleBuilderWithCustomSerDes
        )
    }

    @Test
    fun testEnumQuery() {
        testQuery(
            "enumProperty==VALUE1",
            ExampleBuilderWithCustomSerDes.create().enumProperty().eq(DemoEnum.VALUE1),
            ExampleBuilderWithCustomSerDes
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
}

fun <T : Builder<T>> testQuery(expectedRSQL: String, q: Condition<T>, companion: BuilderCompanion<T>) {
    // Test query builder and serialization
    val rsql = q.toString()
    logger.debug { "Constructed query: $rsql" }
    assertEquals(expectedRSQL, rsql)

    // Test query parsing (deserialization)
    val deserializedQ = companion.parse(rsql)
    assertEquals(q, deserializedQ)
}