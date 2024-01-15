package com.github.idlabdiscover.rsqlutils.builder

import com.github.idlabdiscover.rsqlutils.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BuilderTest {

    @Test
    fun testComposedQuery() {
        val expected = "stringProperty==test;doubleProperty=gt=20.0;booleanProperty==false"
        // Test query builder and serialization
        val q = ExampleBuilder.create().stringProperty().eq("test").and().doubleProperty().gt(20.0).and().booleanProperty().isFalse()
        val rsql = q.toString()
        assertEquals(expected, rsql)

        // Test query parsing (deserialization)
        val deserializedQ = ExampleBuilder.parse(rsql)
        assertEquals(q, deserializedQ)
    }

}

interface ExampleBuilder : Builder<ExampleBuilder> {

    companion object : BuilderCompanion<ExampleBuilder>(ExampleBuilder::class)

    fun stringProperty(): StringProperty<ExampleBuilder>

    fun doubleProperty(): DoubleProperty<ExampleBuilder>

    fun floatProperty(): DoubleProperty<ExampleBuilder>

    fun intProperty(): IntegerProperty<ExampleBuilder>

    fun longProperty(): LongProperty<ExampleBuilder>

    fun shortProperty(): ShortProperty<ExampleBuilder>

    fun booleanProperty(): BooleanProperty<ExampleBuilder>

}