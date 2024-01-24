package com.github.idlabdiscover.rsqlutils.builder

import com.github.idlabdiscover.rsqlutils.model.EquitableProperty
import com.github.idlabdiscover.rsqlutils.model.PropertyHelper
import com.github.idlabdiscover.rsqlutils.serdes.PropertyValueSerDes
import org.junit.jupiter.api.Test

class ClassHierarchyTest {

    @Test
    fun testSingleSerDesForHierarchy() {
        testQuery(
            "attribute1=='ChildClassA,abc,def';attribute2=='ChildClassA,ghi,jkl';attribute3=='ChildClassB,mno,pqr'",
            ClassHierarchyFilter.create().attribute1().eq(ChildClassA("abc", "def")).and().attribute2()
                .eq(ChildClassA("ghi", "jkl")).attribute3().eq(ChildClassB("mno", "pqr")),
            ClassHierarchyFilter
        )
    }

}

interface ClassHierarchyFilter : Builder<ClassHierarchyFilter> {
    companion object : BuilderCompanion<ClassHierarchyFilter>(
        ClassHierarchyFilter::class.java,
        mapOf(ParentClassProperty::class.java to ParentClassPropertyValueSerDes)
    )

    fun attribute1(): ParentClassProperty<ParentClass>
    fun attribute2(): ChildClassAProperty
    fun attribute3(): ChildClassBProperty

}

interface ParentClass {
    val property1: String
    val property2: String
}

data class ChildClassA(override val property1: String, override val property2: String) : ParentClass
data class ChildClassB(override val property1: String, override val property2: String) : ParentClass

open class ParentClassProperty<T : ParentClass>(propertyHelper: PropertyHelper<ClassHierarchyFilter, T>) :
    EquitableProperty<ClassHierarchyFilter, T> by propertyHelper

class ChildClassAProperty(propertyHelper: PropertyHelper<ClassHierarchyFilter, ChildClassA>) :
    ParentClassProperty<ChildClassA>(propertyHelper)

class ChildClassBProperty(propertyHelper: PropertyHelper<ClassHierarchyFilter, ChildClassB>) :
    ParentClassProperty<ChildClassB>(propertyHelper)

object ParentClassPropertyValueSerDes : PropertyValueSerDes<ParentClass> {
    override fun serialize(value: ParentClass): String {
        return listOf(value::class.simpleName, value.property1, value.property2).joinToString(",", "'", "'")
    }

    override fun deserialize(representation: String): ParentClass {
        val values = representation.split(",")
        return when (values[0]) {
            ChildClassA::class.simpleName -> ChildClassA(values[1], values[2])
            ChildClassB::class.simpleName -> ChildClassB(values[1], values[2])
            else -> throw IllegalArgumentException()
        }
    }

}