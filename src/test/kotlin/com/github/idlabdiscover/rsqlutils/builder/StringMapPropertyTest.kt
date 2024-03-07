package com.github.idlabdiscover.rsqlutils.builder

import org.junit.jupiter.api.Test
import com.github.idlabdiscover.rsqlutils.model.StringMapProperty

class StringMapPropertyTest {

    @Test
    fun testKeyExists() {
        testQuery("tags.someKey=ex=true", KVTestQuery.create().tags().entry("someKey").exists(), KVTestQuery)
    }

    @Test
    fun testValueEquals() {
        testQuery("tags.someOtherKey==test", KVTestQuery.create().tags().entry("someOtherKey").eq("test"), KVTestQuery)
    }

}

interface KVTestQuery : Builder<KVTestQuery> {

    companion object : BuilderCompanion<KVTestQuery>(KVTestQuery::class.java)

    fun tags(): StringMapProperty<KVTestQuery>

}