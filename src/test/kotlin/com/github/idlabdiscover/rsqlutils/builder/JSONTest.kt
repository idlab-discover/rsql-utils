package com.github.idlabdiscover.rsqlutils.builder

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.idlabdiscover.rsqlutils.builder.utils.ExampleQuery
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import io.vertx.core.json.Json
import io.vertx.core.json.jackson.DatabindCodec
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

private val logger = KotlinLogging.logger {}
private val example = Example(TestQuery.create().lastName().eq("Doe").and().age().gt(20))

class JSONTest {
    companion object {

        @JvmStatic
        @BeforeAll
        fun registerKotlinModule() {
            DatabindCodec.mapper().registerModule(KotlinModule.Builder().build())
        }

    }

    @Test
    fun testNativeJacksonFails() {
        val json = Json.encode(example)
        assertFails {
            Json.decodeValue(json, BuilderProxy::class.java)
        }
    }

    @Test
    fun testJacksonWithRSQLModuleSucceeds() {
        DatabindCodec.mapper()
            .registerModules(TestQuery.generateJacksonModule(), ExampleQuery.generateJacksonModule())
        val json = Json.encode(example)
        logger.debug { "JSON output: $json" }
        val result = Json.decodeValue(json, Example::class.java)
        assertEquals(example, result)

        // The goal of this second check is to see if we can support multiple query types simultaneously.
        val example2 = Example2(ExampleQuery.create())
        val json2 = Json.encode(example2)
        logger.debug { "JSON output for second test: $json2" }
        val result2 = Json.decodeValue(json2, Example2::class.java)
        assertEquals(example2, result2)

    }

}

data class Example(val filter: TestQuery)
data class Example2(val filter: ExampleQuery)