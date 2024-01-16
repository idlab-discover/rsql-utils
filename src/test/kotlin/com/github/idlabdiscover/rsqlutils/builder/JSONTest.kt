package com.github.idlabdiscover.rsqlutils.builder

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import com.github.idlabdiscover.rsqlutils.model.Condition
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
        DatabindCodec.mapper().registerModule(TestQuery.generateJacksonModule())
        val json = Json.encode(example)
        logger.debug { "JSON output: $json" }
        val result = Json.decodeValue(json, Example::class.java)
        assertEquals(example, result)
    }

}

data class Example(val filter: Condition<TestQuery>)