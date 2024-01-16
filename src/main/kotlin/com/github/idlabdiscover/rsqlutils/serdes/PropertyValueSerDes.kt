package com.github.idlabdiscover.rsqlutils.serdes

import com.github.idlabdiscover.rsqlutils.model.*
import org.apache.commons.text.StringEscapeUtils
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface PropertyValueSerDes<S> {

    companion object {

        val defaults: Map<Class<out Property<*>>, PropertyValueSerDes<out Any>> = mapOf(
            StringProperty::class.java to StringPropertyValueSerDes,
            BooleanProperty::class.java to BooleanPropertyValueSerDes,
            DoubleProperty::class.java to DoublePropertyValueSerDes,
            FloatProperty::class.java to FloatPropertyValueSerDes,
            IntegerProperty::class.java to IntegerPropertyValueSerDes,
            LongProperty::class.java to LongPropertyValueSerDes,
            ShortProperty::class.java to ShortPropertyValueSerDes,
            InstantProperty::class.java to InstantPropertyValueSerDes
        )

    }

    fun serialize(value: S): String

    fun deserialize(representation: String): S
}

abstract class AbstractPropertyValueSerDes<S>(
    private val serializer: (S) -> String,
    private val deserializer: (String) -> S
) : PropertyValueSerDes<S> {

    override fun serialize(value: S): String = serializer.invoke(value)

    override fun deserialize(representation: String): S = deserializer.invoke(representation)

}

object StringPropertyValueSerDes : PropertyValueSerDes<String> {
    override fun serialize(value: String): String {
        return if (value.any { it.isWhitespace() }) {
            "\"${StringEscapeUtils.escapeJava(value)}\""
        } else {
            value
        }
    }

    override fun deserialize(representation: String): String {
        return representation
    }

}

object BooleanPropertyValueSerDes : AbstractPropertyValueSerDes<Boolean>({ it.toString() }, { it.toBoolean() })

object DoublePropertyValueSerDes : AbstractPropertyValueSerDes<Number>({ it.toString() }, { it.toDouble() })


object FloatPropertyValueSerDes : AbstractPropertyValueSerDes<Number>({ it.toString() }, { it.toFloat() })
object IntegerPropertyValueSerDes : AbstractPropertyValueSerDes<Number>({ it.toString() }, { it.toInt() })
object LongPropertyValueSerDes : AbstractPropertyValueSerDes<Number>({ it.toString() }, { it.toLong() })
object ShortPropertyValueSerDes : AbstractPropertyValueSerDes<Number>({ it.toString() }, { it.toShort() })
object InstantPropertyValueSerDes : AbstractPropertyValueSerDes<Instant>({
    it.atOffset(ZoneOffset.UTC).format(
        DateTimeFormatter.ISO_ZONED_DATE_TIME
    )
}, { OffsetDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant() })