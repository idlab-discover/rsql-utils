package com.github.idlabdiscover.rsqlutils.builder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.idlabdiscover.rsqlutils.builder.utils.ExampleQuery
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}
private val example = Example(TestQuery.create().lastName().eq("Doe").and().age().gt(20))

class JSONTest {

    private val mapper = ObjectMapper().registerModules(
        KotlinModule.Builder().build(),
        TestQuery.generateJacksonModule(),
        ExampleQuery.generateJacksonModule()
    )

    @Test
    fun testJacksonWithRSQLModuleSucceeds() {
        val json = mapper.writeValueAsString(example)
        logger.debug { "JSON output: $json" }
        val result = mapper.readValue(json, Example::class.java)
        assertEquals(example, result)

        // The goal of this second check is to see if we can support multiple query types simultaneously.
        val example2 = Example2(ExampleQuery.create())
        val json2 = mapper.writeValueAsString(example2)
        logger.debug { "JSON output for second test: $json2" }
        val result2 = mapper.readValue(json2, Example2::class.java)
        assertEquals(example2, result2)

    }

}

data class Example(val filter: TestQuery)
data class Example2(val filter: ExampleQuery)