/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.ktype

import org.jspecify.annotations.Nullable
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.javaType

abstract class TypeCapture<T> {

  val type: Type
    get() {
      if (this::class.java.isKotlinClass()) {
        val kType = this::class.supertypes.first().arguments[0].type
        return kType?.javaType ?: Any::class.java
      }
      return (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

  val annotatedType: AnnotatedType
    get() {
      if (this::class.java.isKotlinClass()) {
        val kType = this::class.supertypes.first().arguments[0].type
        val type = kType?.javaType ?: Any::class.java
        return AnnotatedTypeImpl(type, kType?.allAnnotations() ?: emptyList())
      }
      return (this::class.java.annotatedSuperclass as AnnotatedParameterizedType)
        .annotatedActualTypeArguments[0]
    }

  fun toKType(): KType = annotatedType.toKType()
}

private fun Class<*>.isKotlinClass(): Boolean =
  getAnnotation(Metadata::class.java) != null

private fun KType.allAnnotations(): List<Annotation> {
  val annotations = annotations
  if (isMarkedNullable)
    return annotations + listOf(Nullable())
  return annotations
}

private class AnnotatedTypeImpl(
  private val type: Type,
  private val annotations: List<Annotation>
) : AnnotatedType {

  override fun getType(): Type = type
  override fun getAnnotations(): Array<Annotation> = annotations.toTypedArray()
  override fun getDeclaredAnnotations(): Array<Annotation> = annotations.toTypedArray()

  override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? =
    annotations.filterIsInstance(annotationClass).firstOrNull()

  override fun toString(): String = type.toString()
}
