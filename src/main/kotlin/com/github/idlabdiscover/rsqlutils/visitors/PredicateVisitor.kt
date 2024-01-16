package com.github.idlabdiscover.rsqlutils.visitors

import com.github.idlabdiscover.rsqlutils.model.*
import cz.jirutka.rsql.parser.ast.RSQLOperators
import org.apache.commons.lang3.reflect.FieldUtils
import java.util.function.Predicate

open class PredicateVisitor<E> : NodeVisitor<Predicate<E>, Void>() {
    override fun visit(node: AndNode, parent: LogicalNode?, context: Void?): Predicate<E> {
        return Predicate { entity -> node.children.map { visitAny(it, node, context) }.all { it.test(entity) } }
    }

    override fun visit(node: OrNode, parent: LogicalNode?, context: Void?): Predicate<E> {
        return Predicate { entity -> node.children.map { visitAny(it, node, context) }.any { it.test(entity) } }
    }

    override fun visit(node: ComparisonNode, parent: LogicalNode?, context: Void?): Predicate<E> {
        val operatorValue = if (node.operator.isMultiValue) node.values else node.values.firstOrNull()
        return when (node.operator) {
            RSQLOperators.EQUAL -> Predicate { entity ->
                operatorValue == extractValue(
                    node.field,
                    entity as Any?
                )
            }

            RSQLOperators.NOT_EQUAL -> Predicate { entity ->
                operatorValue != extractValue(
                    node.field,
                    entity as Any?
                )
            }

            RSQLOperators.LESS_THAN -> Predicate { entity ->
                extractValue(
                    node.field,
                    entity as Any?
                ) as Comparable<Any> < operatorValue as Comparable<Any>
            }

            RSQLOperators.LESS_THAN_OR_EQUAL -> Predicate { entity ->
                extractValue(
                    node.field,
                    entity as Any?
                ) as Comparable<Any> <= operatorValue as Comparable<Any>
            }

            RSQLOperators.GREATER_THAN -> Predicate { entity ->
                extractValue(
                    node.field,
                    entity as Any?
                ) as Comparable<Any> > operatorValue as Comparable<Any>
            }

            RSQLOperators.GREATER_THAN_OR_EQUAL -> Predicate { entity ->
                extractValue(
                    node.field,
                    entity as Any?
                ) as Comparable<Any> >= operatorValue as Comparable<Any>
            }

            RSQLOperators.IN -> inPredicate(operatorValue, node)

            RSQLOperators.NOT_IN -> inPredicate(operatorValue, node).negate()

            AdditionalBasicOperators.EX -> Predicate { entity ->
                FieldUtils.getField(
                    entity!!::class.java,
                    node.field.segments.last()
                ) != null
            }

            AdditionalBasicOperators.RE -> Predicate { entity ->
                Regex.fromLiteral(operatorValue.toString()).matches(
                    extractValue(
                        node.field,
                        entity as Any?
                    ).toString()
                )
            }

            else -> throw UnsupportedOperationException("PredicateVisitor does not support operator '${node.operator.symbol}'")
        }
    }

    private fun inPredicate(
        operatorValue: Any?,
        node: ComparisonNode
    ) = Predicate<E> { entity ->
        operatorValue as Collection<Any>
        operatorValue.contains(
            extractValue(
                node.field,
                entity as Any?
            )
        )
    }

    private fun extractValue(fieldPath: FieldPath, entity: Any?): Any? {
        if (entity == null) return null
        val propertyVal = FieldUtils.readField(entity, fieldPath.segments.first(), true) ?: return null
        return if (fieldPath.segments.size > 1) extractValue(
            FieldPath(
                fieldPath.segments.drop(1)
            ), propertyVal
        ) else propertyVal
    }

}