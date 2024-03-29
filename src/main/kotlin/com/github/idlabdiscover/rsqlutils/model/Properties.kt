package com.github.idlabdiscover.rsqlutils.model

import cz.jirutka.rsql.parser.ast.ComparisonOperator
import cz.jirutka.rsql.parser.ast.RSQLOperators
import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import java.time.Instant

interface Property<T : Builder<T>>

interface ComposedProperty

interface StringMapProperty<T : Builder<T>> : ComposedProperty {

    fun entry(key: String): StringProperty<T>

}

interface ListableProperty<T : Builder<T>, S> : Property<T> {

    fun valueIn(vararg values: S): T = valueIn(values.toList())

    fun valueIn(values: Collection<S>): T

    fun valueNotIn(vararg values: S): T = valueNotIn(values.toList())

    fun valueNotIn(values: Collection<S>): T

}

interface ExistentialProperty<T : Builder<T>> : Property<T> {

    fun exists(): T

    fun doesNotExist(): T

}

interface EquitableProperty<T : Builder<T>, S> : ExistentialProperty<T> {

    fun eq(value: S): T

    fun ne(value: S): T

}

interface InstantLikeProperty<T : Builder<T>, S> : EquitableProperty<T, S> {

    fun before(instant: S, exclusive: Boolean = true): T

    fun after(instant: S, exclusive: Boolean = true): T

    fun between(after: S, before: S, exclusiveAfter: Boolean = true, exclusiveBefore: Boolean = true): T

}

interface NumberProperty<T : Builder<T>, S : Number> : EquitableProperty<T, S>, ListableProperty<T, S> {

    fun gt(number: S): T

    fun lt(number: S): T

    fun gte(number: S): T

    fun lte(number: S): T

}

class PropertyHelper<T : Builder<T>, S>(
    private val propertyClass: Class<Property<*>>,
    private val currentBuilder: BuilderProxy<T>,
    private val field: FieldPath
) :
    EquitableProperty<T, S>, ListableProperty<T, S>, InstantLikeProperty<T, S> {

    fun condition(operator: ComparisonOperator, values: Collection<*>): T {
        val newNode = ComparisonNode(field, operator, values, propertyClass)

        return BuilderProxy.create(
            currentBuilder.builderClass,
            currentBuilder.builderConfig,
            currentBuilder.node.append(newNode),
            currentBuilder.classLoader
        )
    }

    override fun eq(value: S) = condition(RSQLOperators.EQUAL, listOf(value))

    override fun ne(value: S) = condition(RSQLOperators.NOT_EQUAL, listOf(value))
    override fun valueIn(values: Collection<S>) = condition(RSQLOperators.IN, values)

    override fun valueNotIn(values: Collection<S>) = condition(RSQLOperators.NOT_IN, values)

    override fun before(instant: S, exclusive: Boolean) =
        condition(if (exclusive) RSQLOperators.LESS_THAN else RSQLOperators.LESS_THAN_OR_EQUAL, listOf(instant))

    override fun after(instant: S, exclusive: Boolean) =
        condition(if (exclusive) RSQLOperators.GREATER_THAN else RSQLOperators.GREATER_THAN_OR_EQUAL, listOf(instant))

    override fun between(after: S, before: S, exclusiveAfter: Boolean, exclusiveBefore: Boolean) =
        currentBuilder.and(after(after, exclusiveAfter), before(before, exclusiveBefore))

    override fun exists(): T = condition(AdditionalBasicOperators.EX, listOf(true))

    override fun doesNotExist(): T = condition(AdditionalBasicOperators.EX, listOf(false))
}

class StringProperty<T : Builder<T>>(private val helper: PropertyHelper<T, String>) :
    EquitableProperty<T, String> by helper, ListableProperty<T, String> by helper {

    fun lexicallyAfter(value: String) = helper.condition(RSQLOperators.GREATER_THAN, listOf(value))

    fun lexicallyBefore(value: String) = helper.condition(RSQLOperators.LESS_THAN, listOf(value))

    fun lexicallyNotAfter(value: String) = helper.condition(RSQLOperators.LESS_THAN_OR_EQUAL, listOf(value))

    fun lexicallyNotBefore(value: String) = helper.condition(RSQLOperators.GREATER_THAN_OR_EQUAL, listOf(value))

    fun pattern(pattern: String) = helper.condition(AdditionalBasicOperators.RE, listOf(pattern))

}

class BooleanProperty<T : Builder<T>>(private val helper: PropertyHelper<T, Boolean>) :
    ExistentialProperty<T> by helper {

    fun isTrue() = helper.condition(RSQLOperators.EQUAL, listOf(true))

    fun isFalse() = helper.condition(RSQLOperators.EQUAL, listOf(false))

}

abstract class AbstractNumberProperty<T : Builder<T>, S : Number>(private val helper: PropertyHelper<T, S>) :
    NumberProperty<T, S>, EquitableProperty<T, S> by helper, ListableProperty<T, S> by helper {

    override fun gt(number: S) = helper.condition(RSQLOperators.GREATER_THAN, listOf(number))

    override fun lt(number: S) = helper.condition(RSQLOperators.LESS_THAN, listOf(number))

    override fun gte(number: S) = helper.condition(RSQLOperators.GREATER_THAN_OR_EQUAL, listOf(number))

    override fun lte(number: S) = helper.condition(RSQLOperators.LESS_THAN_OR_EQUAL, listOf(number))

}

class DoubleProperty<T : Builder<T>>(helper: PropertyHelper<T, Double>) : AbstractNumberProperty<T, Double>(helper)

class FloatProperty<T : Builder<T>>(helper: PropertyHelper<T, Float>) : AbstractNumberProperty<T, Float>(helper)

class IntegerProperty<T : Builder<T>>(helper: PropertyHelper<T, Int>) : AbstractNumberProperty<T, Int>(helper)

class LongProperty<T : Builder<T>>(helper: PropertyHelper<T, Long>) : AbstractNumberProperty<T, Long>(helper)

class ShortProperty<T : Builder<T>>(helper: PropertyHelper<T, Short>) : AbstractNumberProperty<T, Short>(helper)

abstract class EnumProperty<T : Builder<T>, E : Enum<E>>(helper: PropertyHelper<T, E>) :
    ListableProperty<T, E> by helper,
    EquitableProperty<T, E> by helper

class InstantProperty<T : Builder<T>>(helper: PropertyHelper<T, Instant>) : InstantLikeProperty<T, Instant> by helper