package com.github.idlabdiscover.rsqlutils.impl

import com.github.idlabdiscover.rsqlutils.builder.Builder
import com.github.idlabdiscover.rsqlutils.model.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ComposedPropertyProxy<T : Builder<T>, T2 : ComposedProperty>(
    private val parent: BuilderProxy<T>,
    private val parentField: FieldPath,
    private val propertyClass: Class<T2>
) :
    InvocationHandler {

    companion object {

        fun <T : ComposedProperty> create(parent: BuilderProxy<*>, propertyClass: Class<T>, field: FieldPath): T {
            val composedPropertyProxy = ComposedPropertyProxy(parent, field, propertyClass)
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
                val fieldName =
                    if (StringMapProperty::class.java.isAssignableFrom(propertyClass)) args!![0].toString() else method.name
                method.returnType.getDeclaredConstructor(PropertyHelper::class.java)
                    .newInstance(
                        PropertyHelper<T, Any>(
                            method.returnType as Class<Property<*>>,
                            parent,
                            FieldPath(parentField, fieldName)
                        )
                    )
            }

            else -> throw UnsupportedOperationException()
        }
    }

}