package com.github.idlabdiscover.rsqlutils.visitors

import com.github.idlabdiscover.rsqlutils.builder.BuilderConfig
import com.github.idlabdiscover.rsqlutils.model.*
import com.github.idlabdiscover.rsqlutils.serdes.PropertyValueSerDes

class RSQLVisitor(private val builderConfig: BuilderConfig) : NodeVisitor<String, Void>() {
    override fun visit(node: AndNode, parent: LogicalNode?, context: Void?): String {
        val (prefix, suffix) = if (parent != null) "(" to ")" else "" to ""
        return node.children.joinToString(";", prefix, suffix) { visitAny(it, node, context) }
    }

    override fun visit(node: OrNode, parent: LogicalNode?, context: Void?): String {
        val (prefix, suffix) = if (parent != null) "(" to ")" else "" to ""
        return node.children.joinToString(",", prefix, suffix) { visitAny(it, node, context) }
    }

    override fun visit(node: ComparisonNode, parent: LogicalNode?, context: Void?): String {
        return when {
            node.operator == AdditionalBasicOperators.EX -> "${node.field}${node.operator.symbol}${node.values.first()}"
            node.operator.isMultiValue -> {
                val serializedArgList =
                    node.values.filterNotNull().joinToString(",", "(", ")") { serialize(it, node) }
                "${node.field}${node.operator.symbol}$serializedArgList"
            }

            else -> {
                val rawValue =
                    node.values.firstOrNull() ?: throw IllegalArgumentException("Condition value cannot be null!")
                "${node.field}${node.operator.symbol}${serialize(rawValue, node)}"
            }
        }
    }

    private fun serialize(value: Any, node: ComparisonNode): String {
        val propertySerDes = builderConfig.getPropertySerDes(node.propertyClass)
        val strVal =
            propertySerDes?.serialize(value) ?: value.toString() // Fallback to toString if no SerDes is defined.
        return strVal
    }

}