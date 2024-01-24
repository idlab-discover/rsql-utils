package com.github.idlabdiscover.rsqlutils.builder

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import com.github.idlabdiscover.rsqlutils.model.NodeVisitor
import com.github.idlabdiscover.rsqlutils.model.Property
import com.github.idlabdiscover.rsqlutils.serdes.PropertyValueSerDes
import com.github.idlabdiscover.rsqlutils.visitors.ConditionVisitor
import cz.jirutka.rsql.parser.RSQLParser
import cz.jirutka.rsql.parser.ast.ComparisonOperator
import cz.jirutka.rsql.parser.ast.RSQLOperators
import java.util.function.Predicate

/**
 * Extend this Builder interface with an interface that defines your RSQL builder using methods that return
 * concrete implementations of the Property interface.
 */
interface Builder<T : Builder<T>> {

    fun and(c1: T, c2: T, vararg cn: T): T

    fun or(c1: T, c2: T, vararg cn: T): T

    fun and(conditions: List<T>): T

    fun or(conditions: List<T>): T

    fun and(): T

    fun or(): T

    fun <Q, S> visitUsing(visitor: NodeVisitor<Q, S>, context: S? = null): Q

    fun <E> asPredicate(): Predicate<E>

}

/**
 * The companion object of your builder interface should extend this BuilderCompanion.
 */
open class BuilderCompanion<T : Builder<T>> private constructor(
    val builderClass: Class<T>,
    val builderConfig: BuilderConfig
) {

    @JvmOverloads
    constructor(
        builderClass: Class<T>,
        propertySerDesMappings: Map<Class<out Property<*>>, PropertyValueSerDes<*>> = emptyMap(),
        extraOperators: Set<ComparisonOperator> = emptySet()
    ) : this(
        builderClass, BuilderConfig(
            PropertyValueSerDes.defaults.plus(propertySerDesMappings),
            RSQLOperators.defaultOperators().plus(extraOperators)
        )
    )

    @JvmOverloads
    fun create(classLoader: ClassLoader = Thread.currentThread().contextClassLoader): T {
        return BuilderProxy.create(builderClass, builderConfig, classLoader = classLoader)
    }

    @JvmOverloads
    fun parse(rsql: String, classLoader: ClassLoader = Thread.currentThread().contextClassLoader): T {
        return if (rsql.isBlank()) create() else RSQLParser(builderConfig.operators).parse(rsql)
            .accept(ConditionVisitor(this, classLoader))
    }

    fun generateJacksonModule(): Module {
        val module = SimpleModule()
        module.addSerializer(builderClass, object : JsonSerializer<T>() {
            override fun serialize(instance: T, generator: JsonGenerator, provider: SerializerProvider) {
                generator.writeString(instance.toString())
            }
        })
        module.addDeserializer(builderClass, object : JsonDeserializer<T>() {
            override fun deserialize(parser: JsonParser, context: DeserializationContext): T {
                val rsql = parser.valueAsString
                return this@BuilderCompanion.parse(rsql)
            }

        })
        return module
    }

}

data class BuilderConfig(
    val propertySerDesMapping: Map<Class<out Property<*>>, PropertyValueSerDes<*>>,
    val operators: Set<ComparisonOperator>
) {

    fun getPropertySerDes(propertyClass: Class<Property<*>>): PropertyValueSerDes<Any>? {
        return (propertySerDesMapping[propertyClass] ?: propertySerDesMapping.filterKeys {
            it.isAssignableFrom(
                propertyClass
            )
        }.values.firstOrNull()) as PropertyValueSerDes<Any>?
    }

}

@JvmOverloads
fun <T : Builder<T>> queryBuilderOf(
    builderClass: Class<T>,
    propertySerDesMappings: Map<Class<out Property<*>>, PropertyValueSerDes<*>> = emptyMap(),
    extraOperators: Set<ComparisonOperator> = emptySet()
): BuilderCompanion<T> {
    return BuilderCompanion(builderClass, propertySerDesMappings, extraOperators)
}