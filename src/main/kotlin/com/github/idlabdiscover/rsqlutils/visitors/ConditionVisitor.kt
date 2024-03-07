package com.github.idlabdiscover.rsqlutils.visitors

import cz.jirutka.rsql.parser.ast.*
import cz.jirutka.rsql.parser.ast.AndNode
import cz.jirutka.rsql.parser.ast.ComparisonNode
import cz.jirutka.rsql.parser.ast.OrNode
import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.builder.BuilderCompanion
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import com.github.idlabdiscover.rsqlutils.model.*

class ConditionVisitor<T : Builder<T>>(
    private val builderCompanion: BuilderCompanion<T>,
    private val classLoader: ClassLoader
) :
    NoArgRSQLVisitorAdapter<T>() {
    override fun visit(node: AndNode): T {
        return builderCompanion.create().and(node.children.map { visitAny(it) })
    }

    override fun visit(node: OrNode): T {
        return builderCompanion.create().or(node.children.map { visitAny(it) })
    }

    override fun visit(node: ComparisonNode): T {
        val propertyClass = getPropertyClass(FieldPath(node.selector))
        val propertySerDes = builderCompanion.builderConfig.getPropertySerDes(propertyClass)
            ?: throw IllegalArgumentException("Cannot deserialize '${node.selector}': no property SerDes found for '${propertyClass.simpleName}'")
        return PropertyHelper<T, Any>(
            propertyClass,
            BuilderProxy(
                builderCompanion.builderClass,
                builderCompanion.builderConfig,
                com.github.idlabdiscover.rsqlutils.model.AndNode(),
                classLoader
            ),
            FieldPath(node.selector)
        ).condition(node.operator, node.arguments.map {
            if (node.operator == AdditionalBasicOperators.EX) {
                it.toBoolean()
            } else {
                propertySerDes.deserialize(it)
            }
        })
    }

    private fun visitAny(node: Node): T {
        return when (node) {
            is AndNode -> visit(node)
            is OrNode -> visit(node)
            is ComparisonNode -> visit(node)
            else -> throw IllegalArgumentException("Cannot interpret unknown AST type '${node::class.simpleName}'")
        }
    }

    private fun getPropertyClass(
        fieldPath: FieldPath,
        containingClass: Class<*> = builderCompanion.builderClass,
    ): Class<Property<*>> {
        val propertyClass =
            containingClass.declaredMethods.first { it.name == fieldPath.segments.first() }.returnType as Class<Property<*>>
        return when {
            StringMapProperty::class.java.isAssignableFrom(propertyClass) -> StringProperty::class.java as Class<Property<*>>
            ComposedProperty::class.java.isAssignableFrom(propertyClass) -> getPropertyClass(
                FieldPath(
                    fieldPath.segments.drop(
                        1
                    )
                ), propertyClass
            )

            else -> propertyClass
        }
    }
}