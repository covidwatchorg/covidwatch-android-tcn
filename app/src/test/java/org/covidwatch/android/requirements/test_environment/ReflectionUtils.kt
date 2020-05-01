package org.covidwatch.android.requirements.test_environment

import java.lang.reflect.Field

inline operator fun <reified T> T.set(fieldName: String, value: Any?) {
    val field = T::class.java.getRecursiveField(fieldName)
    field.isAccessible = true
    field.set(this@set, value)
}

inline operator fun <reified T, R> T.get(fieldName: String): R {
    val field = T::class.java.getRecursiveField(fieldName)
    field.isAccessible = true
    return field.get(this@get) as R
}

fun <T> Class<T>.getRecursiveField(name: String): Field =
    declaredFields.singleOrNull { it.name == name }.let {
        if (it != null) it
        else superclass?.getRecursiveField(name) ?: throw RuntimeException("field $name nnot found")
    }
