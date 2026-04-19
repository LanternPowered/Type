/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.ktype

import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.Constructor
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.kotlinFunction

/**
 * Converts the java [Type] to a [KType].
 */
fun Type.toKType(): KType = when (this) {
  is Class<*> -> toKType()
  is ParameterizedType -> toKType()
  is GenericArrayType -> toKType()
  is WildcardType -> toKType()
  is TypeVariable<*> -> toKType()
  else -> throw IllegalStateException("Unexpected type: $this")
}

/**
 * Converts the java [AnnotatedType] to a [KType].
 */
fun AnnotatedType.toKType(): KType = when (this) {
  is AnnotatedParameterizedType -> toKType()
  is AnnotatedArrayType -> toKType()
  is AnnotatedWildcardType -> toKType()
  is AnnotatedTypeVariable -> toKType()
  else -> {
    val type = type
    if (type is Class<*>) {
      type.toKType(isNullable(), annotations.toList())
    } else {
      type.toKType()
    }
  }
}

inline fun <reified A : Annotation> AnnotatedElement.findAnnotation(): A? =
  getAnnotation(A::class.java)

private fun ParameterizedType.toKType(): KType {
  val kClass = (rawType as Class<*>).remapToKotlin()

  val projections = ArrayList<KTypeProjection>()
  collectTypeProjections(projections)

  return kClass.createType(projections)
}

private fun AnnotatedParameterizedType.toKType(): KType {
  val kClass = ((type as ParameterizedType).rawType as Class<*>).remapToKotlin()
  val annotations = annotations.toList()

  val projections = ArrayList<KTypeProjection>()
  collectTypeProjections(projections)

  return kClass.createType(projections, isNullable(), annotations)
}

internal fun Type.collectTypeProjections(
  projections: MutableList<KTypeProjection>
) {
  if (this is ParameterizedType) {
    actualTypeArguments.asSequence()
      .map { it.toTypeProjection() }
      .toCollection(projections)
    ownerType?.collectTypeProjections(projections)
  } else if (this is Class<*>) {
    repeat(typeParameters.size) {
      projections += KTypeProjection.STAR
    }
    enclosingClass?.collectTypeProjections(projections)
  }
}

private fun AnnotatedType.collectTypeProjections(
  projections: MutableList<KTypeProjection>
) {
  if (this is AnnotatedParameterizedType) {
    annotatedActualTypeArguments.asSequence()
      .map { it.toTypeProjection() }
      .toCollection(projections)
  }
  annotatedOwnerType?.collectTypeProjections(projections)
}

private fun Class<*>.toKType(
  nullable: Boolean = false,
  annotations: List<Annotation> = emptyList()
): KType = remapToKotlin().createStarType(nullable, annotations)

private fun GenericArrayType.toKType(): KType {
  val componentType = genericComponentType.toKType()
  val projections = listOf(KTypeProjection.invariant(componentType))
  return Array::class.createType(projections)
}

private fun AnnotatedArrayType.toKType(): KType {
  val componentType = annotatedGenericComponentType.toKType()
  val projections = listOf(KTypeProjection.invariant(componentType))
  val annotations = annotations.toList()
  return Array::class.createType(projections, isNullable(), annotations)
}

private fun WildcardType.toKType(): KType =
  upperBounds.firstOrNull()?.toKType() ?: Any::class.createType()

private fun AnnotatedWildcardType.toKType(): KType =
  annotatedUpperBounds.firstOrNull()?.toKType() ?: Any::class.createType()

private fun TypeVariable<*>.toKType(): KType {
  val typeParameters = getTypeParameters { genericDeclaration }
  val typeParameter = typeParameters.firstOrNull { parameter -> parameter.name == name }
  if (typeParameter != null)
    return typeParameter.createType()
  return annotatedBounds.firstOrNull()?.toKType() ?: Any::class.createType()
}

private fun AnnotatedTypeVariable.toKType(): KType {
  val type = type as TypeVariable<*>
  val typeParameters = getTypeParameters { type.genericDeclaration }
  val typeParameter = typeParameters.firstOrNull { parameter -> parameter.name == type.name }
  if (typeParameter != null) {
    val annotations = annotations.toList()
    return typeParameter.createType(emptyList(), isNullable(), annotations)
  }
  return annotatedBounds.firstOrNull()?.toKType() ?: Any::class.createType()
}

private fun getTypeParameters(declarationSupplier: () -> Any?): List<KTypeParameter> {
  val declaration = try {
    declarationSupplier()
  } catch (_: Exception) {
    return emptyList()
  }
  return when (declaration) {
    is Class<*> -> declaration.kotlin.typeParameters
    is Method -> declaration.kotlinFunction?.typeParameters ?: emptyList()
    is Constructor<*> -> declaration.kotlinFunction?.typeParameters ?: emptyList()
    else -> emptyList()
  }
}

private fun Type.toTypeProjection(): KTypeProjection {
  return when (this) {
    is WildcardType -> toTypeProjection()
    else -> KTypeProjection.invariant(toKType())
  }
}

private fun WildcardType.toTypeProjection(): KTypeProjection {
  val lowerBound = lowerBounds.firstOrNull()
  if (lowerBound != null)
    return KTypeProjection.contravariant(lowerBound.toKType())
  val upperBound = upperBounds.firstOrNull()
  if (upperBound != null && upperBound != Any::class.java)
    return KTypeProjection.covariant(upperBound.toKType())
  return KTypeProjection.STAR
}

private fun AnnotatedType.toTypeProjection(): KTypeProjection {
  return when (this) {
    is AnnotatedWildcardType -> toTypeProjection()
    else -> KTypeProjection.invariant(toKType())
  }
}

private fun AnnotatedWildcardType.toTypeProjection(): KTypeProjection {
  val lowerBound = annotatedLowerBounds.firstOrNull()
  if (lowerBound != null)
    return KTypeProjection.contravariant(lowerBound.toKType())
  val upperBound = annotatedUpperBounds.firstOrNull()
  if (upperBound != null && upperBound.type != Any::class.java)
    return KTypeProjection.covariant(upperBound.toKType())
  return KTypeProjection.STAR
}

private val nullableAnnotations = setOf(
  "org.checkerframework.checker.nullness.qual.Nullable",
  "javax.annotation.Nullable",
  "jakarta.annotation.Nullable",
  "org.jetbrains.annotations.Nullable",
  "org.jspecify.annotations.Nullable"
)

private fun AnnotatedType.isNullable(): Boolean =
  annotations.any { annotation ->
    nullableAnnotations.contains(annotation.annotationClass.java.name)
  }

private fun Class<*>.remapToKotlin(): KClass<*> = when(this) {
  Void::class.java, Void.TYPE -> Unit::class
  else -> this.kotlin
}
