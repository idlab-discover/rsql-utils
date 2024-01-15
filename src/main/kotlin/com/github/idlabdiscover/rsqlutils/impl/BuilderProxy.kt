package com.github.idlabdiscover.rsqlutils.impl

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.builder.BuilderConfig
import com.github.idlabdiscover.rsqlutils.model.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class BuilderProxy<T : Builder<T>>(builderClass: Class<T>, builderConfig: BuilderConfig, node: LogicalNode) :
    AbstractCondition<T>(builderClass, builderConfig, node), InvocationHandler {

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
            LogicalOp.isCombineMethod(method) -> {
                parseConditions(args, method)
            }

            Property::class.java.isAssignableFrom(method.returnType) -> {
                method.returnType.getDeclaredConstructor(PropertyHelper::class.java)
                    .newInstance(PropertyHelper<T, Any>(method.returnType as Class<Property<*>>, this, field))
            }

            ComposedProperty::class.java.isAssignableFrom(method.returnType) -> {
                ComposedPropertyProxy.create(this, method.returnType as Class<out ComposedProperty>, field)
            }

            else -> throw UnsupportedOperationException()
        }
    }

    private fun parseConditions(
        args: Array<out Any?>?,
        method: Method
    ): Condition<T> {
        val conditions =
            if (args!!.size == 1) args.first() as List<Condition<T>> else listOf(
                args[0],
                args[1],
                *(args[2] as Array<*>)
            ).map { it as Condition<T> }
                .toList()
        return combine(conditions, LogicalOp.valueOf(method.name.uppercase()))
    }

}