package com.github.idlabdiscover.rsqlutils.builder;

import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery;
import com.github.idlabdiscover.rsqlutils.builder.utils.TestRecord;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.github.idlabdiscover.rsqlutils.builder.BuilderKt.*;

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
    public void testJacksonModule() {
        var q = queryBuilderOf(TestQuery.class).create().firstName().eq("Jane");
        var example = new Example(q);

        // Register generated Jackson module with mapper
        DatabindCodec.mapper().registerModule(queryBuilderOf(TestQuery.class).generateJacksonModule());
        var json = JsonObject.mapFrom(example);
        assertEquals(q.toString(), json.getString("filter"));
    }

}
