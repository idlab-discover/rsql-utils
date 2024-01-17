package com.github.idlabdiscover.rsqlutils.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery;
import com.github.idlabdiscover.rsqlutils.builder.utils.TestRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.idlabdiscover.rsqlutils.builder.BuilderKt.queryBuilderOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseFromJavaTest {

    private final Logger logger = LoggerFactory.getLogger(UseFromJavaTest.class);

    @Test
    public void testBasicUsage() {
        var q = queryBuilderOf(JavaQuery.class).create().stringProperty().eq("test").or().intProperty().gt(25);
        var rsql = q.toString();
        logger.debug("Constructed query: {}", rsql);

        var parsedQ = queryBuilderOf(JavaQuery.class).parse(rsql);
        assertEquals(q, parsedQ);
    }

    @Test
    public void testPredicateConversion() {
        var testRecord = new TestRecord(0L, "Jane", "Doe", (short) 27, null);
        var predicate = queryBuilderOf(TestQuery.class).create().lastName().eq("Doe").asPredicate();
        assertTrue(predicate.test(testRecord));
    }

    @Test
    public void testJacksonModule() throws JsonProcessingException {
        var q = queryBuilderOf(TestQuery.class).create().firstName().eq("Jane");
        var example = new Example(q);

        // Register generated Jackson module with mapper
        var mapper = new ObjectMapper().registerModules(queryBuilderOf(TestQuery.class).generateJacksonModule());
        var json = mapper.writeValueAsString(example);
        var tree = mapper.readTree(json);
        assertEquals(q.toString(), tree.get("filter").asText());
    }

}
