package com.github.idlabdiscover.rsqlutils.builder

import cz.jirutka.rsql.parser.RSQLParser
import cz.jirutka.rsql.parser.ast.ComparisonOperator
import cz.jirutka.rsql.parser.ast.RSQLOperators
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import com.github.idlabdiscover.rsqlutils.model.*
import com.github.idlabdiscover.rsqlutils.serdes.PropertyValueSerDes
import com.github.idlabdiscover.rsqlutils.visitors.ConditionVisitor
import kotlin.reflect.KClass

/**
 * Extend this Builder interface with an interface that defines your RSQL builder using methods that return
 * concrete implementations of the Property interface.
 */
interface Builder<T : Builder<T>> {

    fun and(c1: Condition<T>, c2: Condition<T>, vararg cn: Condition<T>): Condition<T>

    fun or(c1: Condition<T>, c2: Condition<T>, vararg cn: Condition<T>): Condition<T>

    fun and(conditions: List<Condition<T>>): Condition<T>

    fun or(conditions: List<Condition<T>>): Condition<T>

}

/**
 * The companion object of your builder interface should extend this BuilderCompanion.
 */
abstract class BuilderCompanion<T : Builder<T>> private constructor(
    val builderClass: KClass<T>,
    val builderConfig: BuilderConfig
) {
    constructor(
        builderClass: KClass<T>,
        propertySerDesMappings: Map<Class<out Property<*>>, PropertyValueSerDes<*>> = emptyMap(),
        extraOperators: Set<ComparisonOperator> = emptySet()
    ) : this(
        builderClass, BuilderConfig(
            PropertyValueSerDes.defaults.plus(propertySerDesMappings),
            RSQLOperators.defaultOperators().plus(extraOperators)
        )
    )

    fun create(): T {
        return BuilderProxy.create(builderClass.java, builderConfig)
    }

    fun parse(rsql: String): Condition<T> {
        return RSQLParser(builderConfig.operators).parse(rsql)
            .accept(ConditionVisitor(this))
    }

}

data class BuilderConfig(
    val propertySerDesMapping: Map<Class<out Property<*>>, PropertyValueSerDes<*>>,
    val operators: Set<ComparisonOperator>
) {

    fun getPropertySerDes(propertyClass: Class<Property<*>>): PropertyValueSerDes<Any> {
        return (propertySerDesMapping[propertyClass]
            ?: throw IllegalArgumentException("No property SerDes found for '${propertyClass.simpleName}'")) as PropertyValueSerDes<Any>
    }

}