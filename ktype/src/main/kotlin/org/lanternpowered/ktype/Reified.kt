/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST")

package org.lanternpowered.ktype

import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

/**
 * Represents a [value] with a specific reified [type] that cannot be derived from the [value] class.
 */
data class Reified<out T>(
  val type: KType,
  val value: T
)

/**
 * Returns a [Reified] based on the target and reified type. The reified type will be generated
 * from both the instance type and the reified type information.
 *
 * For example, when reifying an `ArrayList` instance which is stored in a parameter with the type
 * `List<Int>`. The returned reified type will be `ArrayList<Int>`.
 */
inline fun <reified T> T.reify(): Reified<T> = reify(typeOf<T>())

/**
 * Returns a [Reified] based on the target and reified type. The reified type will be generated
 * from both the instance type and the reified type information.
 *
 * For example, when reifying an `ArrayList` instance which is stored in a parameter with the type
 * `List<Int>`. The returned reified type will be `ArrayList<Int>`.
 */
fun <T> T.reify(reifiedType: KType): Reified<T> {
  if (this == null)
    return Reified(reifiedType, null as T)
  @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
  val instanceType = this!!::class.createStarType()
  return Reified(instanceType.reify(reifiedType), this)
}

/**
 * Enriches the target [KType] with the given reified type information.
 *
 * For example, when reifying an `ArrayList` instance which is stored in a parameter with the type
 * `List<Int>`. The returned reified type will be `ArrayList<Int>`.
 */
fun KType.reify(reifiedType: KType): KType {
  val instanceClass = classifier as? KClass<*>
  if (isSupertypeOf(reifiedType) || instanceClass == null)
    return reifiedType
  return resolve(instanceClass.createParameterizedType(), reifiedType)
    // This can only happen if the types are incompatible
    ?: return reifiedType
}

private fun resolve(
  parameterizedType: KType,
  reifiedType: KType
): KType? {
  // Types are already aligned, so we can just return the reified type
  if (reifiedType.classifier == parameterizedType.classifier)
    return reifiedType
  @Suppress("NAME_SHADOWING")
  var reifiedType = reifiedType
  if (!parameterizedType.superclasses.contains(reifiedType.classifier as? KClass<*>)) {
    reifiedType = parameterizedType.supertypes.asSequence()
      .map { supertype -> resolve(supertype, reifiedType) }
      .firstOrNull()
      ?: return null
  }
  val parameterizedSupertype = parameterizedType.supertypes
    .first { it.classifier == reifiedType.classifier }
  val arguments = collectArguments(parameterizedSupertype, reifiedType)
  return parameterizedType.applyArguments(arguments)
}

private fun KType.applyArguments(arguments: Map<String, KType>): KType =
  applyArgumentsOrNull(arguments) ?: this

private fun KType.applyArgumentsOrNull(arguments: Map<String, KType>): KType? {
  return when (val classifier = classifier) {
    null -> null
    is KTypeParameter -> arguments[classifier.name]
    is KClass<*> -> {
      val projections = ArrayList<KTypeProjection>(this.arguments.size)
      var modified = false
      for (projection in this.arguments) {
        val type = projection.type
        if (type == null) {
          projections += projection
        } else {
          val result = type.applyArgumentsOrNull(arguments)
          projections += if (result != null) {
            modified = true
            KTypeProjection.invariant(result)
          } else {
            projection
          }
        }
      }
      if (modified) classifier.createType(projections, isMarkedNullable, annotations) else null
    }
    else -> unexpectedClassifier(classifier)
  }
}

private fun collectArguments(
  parameterizedType: KType,
  reifiedType: KType,
): Map<String, KType> {
  val resolvedParamTypes = HashMap<String, KType>()
  for (index in parameterizedType.arguments.indices) {
    val parameterizedParamType = parameterizedType.arguments[index]
    val reifiedParamType = reifiedType.arguments[index]
    if (parameterizedParamType.type?.classifier is KTypeParameter) {
      val typeParameter = parameterizedParamType.type?.classifier as KTypeParameter
      val resolvedParamType = if (reifiedParamType.type == null) {
        typeParameter.upperBounds.firstOrNull() ?: Any::class.createType()
      } else {
        reifiedParamType.type!!
      }
      resolvedParamTypes[typeParameter.name] = resolvedParamType
    }
  }
  return resolvedParamTypes
}
