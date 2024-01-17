package com.github.idlabdiscover.rsqlutils.impl

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.model.ComposedProperty
import com.github.idlabdiscover.rsqlutils.model.FieldPath
import com.github.idlabdiscover.rsqlutils.model.Property
import com.github.idlabdiscover.rsqlutils.model.PropertyHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ComposedPropertyProxy<T : Builder<T>>(private val parent: BuilderProxy<T>, private val parentField: FieldPath) :
    InvocationHandler {

    companion object {

        fun <T : ComposedProperty> create(parent: BuilderProxy<*>, propertyClass: Class<T>, field: FieldPath): T {
            val composedPropertyProxy = ComposedPropertyProxy(parent, field)
            return propertyClass.cast(
                Proxy.newProxyInstance(
                    propertyClass.classLoader,
                    arrayOf(propertyClass),
                    composedPropertyProxy
                )
            )
        }

    }

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        return when {
            Property::class.java.isAssignableFrom(method.returnType) -> {
                method.returnType.getDeclaredConstructor(PropertyHelper::class.java)
                    .newInstance(
                        PropertyHelper<T, Any>(
                            method.returnType as Class<Property<*>>,
                            parent,
                            FieldPath(parentField, method.name)
                        )
                    )
            }

            else -> throw UnsupportedOperationException()
        }
    }

}