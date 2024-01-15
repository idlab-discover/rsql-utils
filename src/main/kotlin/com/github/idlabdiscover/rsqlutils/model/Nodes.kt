package com.github.idlabdiscover.rsqlutils.model

import cz.jirutka.rsql.parser.ast.ComparisonOperator
import java.util.*

sealed class AbstractNode

sealed class LogicalNode(val children: List<AbstractNode> = listOf()) :
    AbstractNode() {

    abstract fun append(node: AbstractNode): LogicalNode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LogicalNode) return false

        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        return children.hashCode()
    }


}

class AndNode(children: List<AbstractNode> = listOf()) :
    LogicalNode(children) {

    constructor(child: AbstractNode) : this(listOf(child))

    override fun append(node: AbstractNode): LogicalNode {
        return AndNode(children.plus(node))
    }
}

class OrNode(children: List<AbstractNode> = listOf()) :
    LogicalNode(children) {

    constructor(child: AbstractNode) : this(listOf(child))

    override fun append(node: AbstractNode): LogicalNode {
        return OrNode(children.plus(node))
    }

}

class ComparisonNode(
    val field: FieldPath,
    val operator: ComparisonOperator,
    val values: Collection<*>,
    val propertyClass: Class<Property<*>>
) : AbstractNode() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComparisonNode) return false

        if (field != other.field) return false
        if (operator != other.operator) return false
        if (values != other.values) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(field, operator, values, propertyClass)
    }
}

data class FieldPath(val segments: List<String>) {

    constructor(fieldName: String) : this(fieldName.split("."))

    constructor(parentPath: FieldPath, fieldName: String) : this(parentPath.segments.plus(fieldName))

    override fun toString(): String {
        return segments.joinToString(".")
    }

}