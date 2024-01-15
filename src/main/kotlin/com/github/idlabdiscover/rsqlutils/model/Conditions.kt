package com.github.idlabdiscover.rsqlutils.model

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.builder.BuilderConfig
import com.github.idlabdiscover.rsqlutils.impl.BuilderProxy
import com.github.idlabdiscover.rsqlutils.visitors.RSQLVisitor
import java.lang.reflect.Method

interface Condition<T : Builder<T>> : Builder<T> {

    fun and(): T

    fun or(): T

    fun <Q, S> query(visitor: NodeVisitor<Q, S>, context: S? = null): Q

}

abstract class AbstractCondition<T : Builder<T>>(
    val builderClass: Class<T>,
    val builderConfig: BuilderConfig,
    val node: LogicalNode
) :
    Condition<T> {
    override fun and(): T {
        return BuilderProxy.create(
            builderClass, builderConfig, if (node !is AndNode) AndNode(simplifyNode(node)) else node
        )
    }

    override fun or(): T {
        return BuilderProxy.create(
            builderClass, builderConfig, if (node !is OrNode) OrNode(simplifyNode(node)) else node
        )
    }

    override fun or(c1: Condition<T>, c2: Condition<T>, vararg cn: Condition<T>): Condition<T> {
        return or(listOf(c1, c2, *cn))
    }

    override fun or(conditions: List<Condition<T>>): Condition<T> {
        return combine(conditions, LogicalOp.OR)
    }

    override fun and(c1: Condition<T>, c2: Condition<T>, vararg cn: Condition<T>): Condition<T> {
        return and(listOf(c1, c2, *cn))
    }

    override fun and(conditions: List<Condition<T>>): Condition<T> {
        return combine(conditions, LogicalOp.AND)
    }

    override fun <Q, S> query(visitor: NodeVisitor<Q, S>, context: S?): Q {
        return visitor.visitAny(node, context = context)
    }

    protected fun combine(conditions: List<Condition<T>>, operator: LogicalOp): Condition<T> {
        val children: List<AbstractNode> = conditions.map { simplifyNode((it as AbstractCondition).node) }
        val newNode = when (operator) {
            LogicalOp.AND -> AndNode(children)
            LogicalOp.OR -> OrNode(children)
        }
        return BuilderProxy(builderClass, builderConfig, node.append(newNode))
    }

    private fun simplifyNode(node: AbstractNode): AbstractNode {
        return if (node is LogicalNode && node.children.size == 1) node.children[0] else node
    }

    override fun toString(): String {
        return RSQLVisitor(builderConfig).visitAny(node)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractCondition<*>) return false

        if (builderClass != other.builderClass) return false
        if (node != other.node) return false

        return true
    }

    override fun hashCode(): Int {
        var result = builderClass.hashCode()
        result = 31 * result + node.hashCode()
        return result
    }


}

enum class LogicalOp {
    AND, OR;

    companion object {

        fun isCombineMethod(method: Method): Boolean {
            return entries.map { it.name }.contains(method.name.uppercase())
        }

    }
}