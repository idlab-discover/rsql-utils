package com.github.idlabdiscover.rsqlutils.builder.utils

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.builder.BuilderCompanion
import com.github.idlabdiscover.rsqlutils.model.*
import com.github.idlabdiscover.rsqlutils.serdes.AbstractPropertyValueSerDes
import com.github.idlabdiscover.rsqlutils.serdes.PropertyValueSerDes
import java.net.URI
import java.time.Instant

interface ExampleQuery : Builder<ExampleQuery> {

    companion object : BuilderCompanion<ExampleQuery>(ExampleQuery::class.java)

    fun stringProperty(): StringProperty<ExampleQuery>

    fun doubleProperty(): DoubleProperty<ExampleQuery>

    fun floatProperty(): DoubleProperty<ExampleQuery>

    fun intProperty(): IntegerProperty<ExampleQuery>

    fun longProperty(): LongProperty<ExampleQuery>

    fun shortProperty(): ShortProperty<ExampleQuery>

    fun booleanProperty(): BooleanProperty<ExampleQuery>

    fun instantProperty(): InstantProperty<ExampleQuery>

}

interface ExampleQueryWithCustomSerDes : Builder<ExampleQueryWithCustomSerDes> {

    companion object : BuilderCompanion<ExampleQueryWithCustomSerDes>(
        ExampleQueryWithCustomSerDes::class.java,
        mapOf(
            InstantProperty::class.java to CustomInstantSerDes,
            DemoEnumProperty::class.java to DemoEnumPropertyValueSerDes
        )
    )

    fun instantProperty(): InstantProperty<ExampleQueryWithCustomSerDes>

    fun enumProperty(): DemoEnumProperty

}

enum class DemoEnum {
    VALUE1, VALUE2, VALUE3
}

object CustomInstantSerDes :
    AbstractPropertyValueSerDes<Instant>({ it.toEpochMilli().toString() }, { Instant.ofEpochMilli(it.toLong()) })

class DemoEnumProperty(helper: PropertyHelper<ExampleQueryWithCustomSerDes, DemoEnum>) :
    EnumProperty<ExampleQueryWithCustomSerDes, DemoEnum>(helper)

object DemoEnumPropertyValueSerDes : AbstractPropertyValueSerDes<DemoEnum>({ it.toString() }, { DemoEnum.valueOf(it) })

data class TestRecord(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Short,
    val address: TestAddress? = null
)

data class TestAddress(
    val street: String,
    val houseNumber: Int,
    val city: String,
    val postalCode: Int,
    val country: String
)

interface TestQuery : Builder<TestQuery> {
    companion object :
        BuilderCompanion<TestQuery>(TestQuery::class.java, mapOf(URIProperty::class.java to URIPropertyValueSerDes))

    fun id(): LongProperty<TestQuery>
    fun firstName(): StringProperty<TestQuery>
    fun lastName(): StringProperty<TestQuery>
    fun age(): ShortProperty<TestQuery>
    fun address(): AddressProperty

    fun homePage(): URIProperty<TestQuery>
}

interface AddressProperty : ComposedProperty {
    fun street(): StringProperty<TestQuery>
    fun houseNumber(): IntegerProperty<TestQuery>
    fun city(): StringProperty<TestQuery>
    fun postalCode(): IntegerProperty<TestQuery>
    fun country(): StringProperty<TestQuery>
}

class URIProperty<T : Builder<T>>(private val helper: PropertyHelper<T, URI>) :
    EquitableProperty<T, URI> by helper, ListableProperty<T, URI> by helper

object URIPropertyValueSerDes : PropertyValueSerDes<URI> {
    override fun serialize(value: URI): String {
        return value.toASCIIString()
    }

    override fun deserialize(representation: String): URI {
        return URI.create(representation)
    }

}