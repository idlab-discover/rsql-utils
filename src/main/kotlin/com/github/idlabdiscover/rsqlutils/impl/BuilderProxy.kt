package com.github.idlabdiscover.rsqlutils.impl

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.builder.BuilderConfig
import com.github.idlabdiscover.rsqlutils.model.*
import com.github.idlabdiscover.rsqlutils.visitors.RSQLVisitor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class BuilderProxy<T : Builder<T>> internal constructor(
    val builderClass: Class<T>,
    val builderConfig: BuilderConfig,
    val node: LogicalNode
) :
    Builder<T>, InvocationHandler {

    companion object {

        fun <T : Builder<T>> create(
            builderClass: Class<T>,
            builderConfig: BuilderConfig,
            node: LogicalNode = OrNode()
        ): T {
            val builderProxy = BuilderProxy(builderClass, builderConfig, node)
            return builderClass.cast(
                Proxy.newProxyInstance(
                    builderClass.classLoader,
                    arrayOf(builderClass),
                    builderProxy
                )
            )
        }

    }

    override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
        val field = FieldPath(method.name)
        return when {
            Property::class.java.isAssignableFrom(method.returnType) -> {
                method.returnType.getDeclaredConstructor(PropertyHelper::class.java)
                    .newInstance(PropertyHelper<T, Any>(method.returnType as Class<Property<*>>, this, field))
            }

            ComposedProperty::class.java.isAssignableFrom(method.returnType) -> {
                ComposedPropertyProxy.create(this, method.returnType as Class<out ComposedProperty>, field)
            }

            // Forward to the BuildProxy instance
            else -> try {
                if (args != null && args.isNotEmpty()) method.invoke(this, *args) else method.invoke(this)
            } catch (err: Throwable) {
                err.printStackTrace()
            }
        }
    }

    override fun and(): T {
        return create(
            builderClass, builderConfig, if (node !is AndNode) AndNode(simplifyNode(node)) else node
        )
    }

    override fun or(): T {
        return create(
            builderClass, builderConfig, if (node !is OrNode) OrNode(simplifyNode(node)) else node
        )
    }

    override fun or(c1: T, c2: T, vararg cn: T): T {
        return or(listOf(c1, c2, *cn))
    }

    override fun or(conditions: List<T>): T {
        return combine(conditions, LogicalOp.OR)
    }

    override fun and(c1: T, c2: T, vararg cn: T): T {
        return and(listOf(c1, c2, *cn))
    }

    override fun and(conditions: List<T>): T {
        return combine(conditions, LogicalOp.AND)
    }

    override fun <Q, S> visitUsing(visitor: NodeVisitor<Q, S>, context: S?): Q {
        return visitor.visitAny(node, context = context)
    }

    protected fun combine(conditions: List<T>, operator: LogicalOp): T {
        val children: List<AbstractNode> = conditions.map { simplifyNode(accessNodeForProxy(it)) }
            .filter { it !is LogicalNode || it.children.isNotEmpty() }
        val newNode = when (operator) {
            LogicalOp.AND -> AndNode(children)
            LogicalOp.OR -> OrNode(children)
        }
        return create(builderClass, builderConfig, if (children.isNotEmpty()) node.append(newNode) else node)
    }

    private fun simplifyNode(node: AbstractNode): AbstractNode {
        return if (node is LogicalNode && node.children.size == 1) node.children[0] else node
    }

    override fun toString(): String {
        return RSQLVisitor(builderConfig).visitAny(node)
    }

    override fun equals(other: Any?): Boolean {
        val unwrappedOther = other?.let { Proxy.getInvocationHandler(it) } ?: other

        if (this === unwrappedOther) return true
        if (unwrappedOther !is BuilderProxy<*>) return false

        if (builderClass != unwrappedOther.builderClass) return false
        if (simplifyNode(node) != simplifyNode(unwrappedOther.node)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = builderClass.hashCode()
        result = 31 * result + node.hashCode()
        return result
    }
}

private fun <T : Builder<T>> accessNodeForProxy(proxy: T): LogicalNode {
    return (Proxy.getInvocationHandler(proxy) as BuilderProxy<T>).node
}

enum class LogicalOp {
    AND, OR;

    companion object {

        fun isCombineMethod(method: Method): Boolean {
            return entries.map { it.name }.contains(method.name.uppercase())
        }

    }
}