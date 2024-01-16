package com.github.idlabdiscover.rsqlutils.builder

import com.github.idlabdiscover.rsqlutils.builder.utils.TestAddress
import com.github.idlabdiscover.rsqlutils.builder.utils.TestQuery
import com.github.idlabdiscover.rsqlutils.builder.utils.TestRecord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


private val record = TestRecord(0L, "Jane", "Doe", 27)
private val complexRecord = record.copy(address = TestAddress("Rue des Rivoli", 1, "Paris", 75008, "France"))

class PredicateVisitorTest {

    @Test
    fun testRecordMatches() {
        val q = TestQuery.create().firstName().eq("Jane").and().lastName().eq("Doe")
        assertTrue { q.asPredicate<TestRecord>().test(record) }
    }

    @Test
    fun testRecordDoesNotMatch() {
        val q = TestQuery.create().age().gt(50)
        assertFalse { q.asPredicate<TestRecord>().test(record) }
    }

    @Test
    fun testComplexRecordMatches() {
        val q = TestQuery.create().address().street().eq("Rue des Rivoli")
        assertTrue { q.asPredicate<TestRecord>().test(complexRecord) }
    }

    @Test
    fun testComplexRecorDoesNotMatch() {
        val q = TestQuery.create().address().country().eq("Belgium").or().address().country().eq("Germany")
        assertFalse { q.asPredicate<TestRecord>().test(complexRecord) }
    }

    @Test
    fun testCollectionFiltering() {
        val persons = listOf(
            TestRecord(0L, "Jane", "Doe", 27),
            TestRecord(0L, "John", "Doe", 30),
            TestRecord(0L, "Billy", "Doe", 4),
            TestRecord(0L, "Alice", "Doe", 2)
        )

        val q = TestQuery.create().age().gt(20)
        val predicate = q.asPredicate<TestRecord>()
        assertEquals(persons.filter { it.age > 20 }, persons.filter(predicate::test))
    }

}