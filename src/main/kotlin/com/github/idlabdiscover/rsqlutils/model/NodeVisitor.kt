package com.github.idlabdiscover.rsqlutils.model

abstract class NodeVisitor<T, C> {

    protected abstract fun visit(node: AndNode, parent: LogicalNode? = null, context: C? = null): T

    protected abstract fun visit(node: OrNode, parent: LogicalNode? = null, context: C? = null): T

    protected abstract fun visit(node: ComparisonNode, parent: LogicalNode? = null, context: C? = null): T

    fun visitAny(node: AbstractNode, parent: LogicalNode? = null, context: C? = null): T {
        // Skip straight to the children if it's a logical node with one member
        if (node is LogicalNode && node.children.size == 1) {
            return visitAny(node.children.first(), parent, context)
        }

        return when (node) {
            is AndNode -> visit(node, parent, context)
            is OrNode -> visit(node, parent, context)
            is ComparisonNode -> visit(node, parent, context)
        }
    }

}