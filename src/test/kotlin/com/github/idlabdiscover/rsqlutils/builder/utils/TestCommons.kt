package com.github.idlabdiscover.rsqlutils.builder.utils

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.builder.BuilderCompanion
import com.github.idlabdiscover.rsqlutils.model.*
import com.github.idlabdiscover.rsqlutils.serdes.AbstractPropertyValueSerDes
import java.time.Instant

interface ExampleBuilder : Builder<ExampleBuilder> {

    companion object : BuilderCompanion<ExampleBuilder>(ExampleBuilder::class)

    fun stringProperty(): StringProperty<ExampleBuilder>

    fun doubleProperty(): DoubleProperty<ExampleBuilder>

    fun floatProperty(): DoubleProperty<ExampleBuilder>

    fun intProperty(): IntegerProperty<ExampleBuilder>

    fun longProperty(): LongProperty<ExampleBuilder>

    fun shortProperty(): ShortProperty<ExampleBuilder>

    fun booleanProperty(): BooleanProperty<ExampleBuilder>

    fun instantProperty(): InstantProperty<ExampleBuilder>

}

interface ExampleBuilderWithCustomSerDes : Builder<ExampleBuilderWithCustomSerDes> {

    companion object : BuilderCompanion<ExampleBuilderWithCustomSerDes>(
        ExampleBuilderWithCustomSerDes::class,
        mapOf(
            InstantProperty::class.java to CustomInstantSerDes,
            DemoEnumProperty::class.java to DemoEnumPropertyValueSerDes
        )
    )

    fun instantProperty(): InstantProperty<ExampleBuilderWithCustomSerDes>

    fun enumProperty(): DemoEnumProperty

}

enum class DemoEnum {
    VALUE1, VALUE2, VALUE3
}

object CustomInstantSerDes :
    AbstractPropertyValueSerDes<Instant>({ it.toEpochMilli().toString() }, { Instant.ofEpochMilli(it.toLong()) })

class DemoEnumProperty(helper: PropertyHelper<ExampleBuilderWithCustomSerDes, DemoEnum>) :
    EnumProperty<ExampleBuilderWithCustomSerDes, DemoEnum>(helper)

object DemoEnumPropertyValueSerDes : AbstractPropertyValueSerDes<DemoEnum>({ it.toString() }, { DemoEnum.valueOf(it) })

data class TestRecord(val id: Long, val firstName: String, val lastName: String, val age: Short, val address: TestAddress? = null)
data class TestAddress(
    val street: String,
    val houseNumber: Int,
    val city: String,
    val postalCode: Int,
    val country: String
)

interface TestQuery : Builder<TestQuery> {
    companion object : BuilderCompanion<TestQuery>(TestQuery::class)

    fun id(): LongProperty<TestQuery>
    fun firstName(): StringProperty<TestQuery>
    fun lastName(): StringProperty<TestQuery>
    fun age(): ShortProperty<TestQuery>
    fun address(): AddressProperty
}

interface AddressProperty : ComposedProperty {
    fun street(): StringProperty<TestQuery>
    fun houseNumber(): IntegerProperty<TestQuery>
    fun city(): StringProperty<TestQuery>
    fun postalCode(): IntegerProperty<TestQuery>
    fun country(): StringProperty<TestQuery>
}