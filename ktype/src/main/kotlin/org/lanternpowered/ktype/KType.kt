/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.ktype

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.superclasses
import kotlin.reflect.typeOf

private val packagePrefixRegex = "(?:^|[<>,])((?:[a-z][^.]*\\.)+)([^<>,]*)".toRegex()

/**
 * Returns a simple string representation of this [KType].
 */
fun KType.toSimpleString(): String {
  var value = toString()

  while (true) {
    val match = packagePrefixRegex.find(value)
      ?: return value
    value = value.replaceRange(match.groups[1]!!.range, "")
  }
}

/**
 * Returns the superclasses of this [KType].
 */
val KType.superclasses: List<KClass<*>>
  get() = when (val classifier = classifier) {
    null, is KTypeParameter -> emptyList()
    is KClass<*> -> classifier.superclasses
    else -> unexpectedClassifier(classifier)
  }

/**
 * Returns all the superclasses of this [KType].
 */
val KType.allSuperclasses: Collection<KClass<*>>
  get() = when (val classifier = classifier) {
    null, is KTypeParameter -> emptyList()
    is KClass<*> -> classifier.allSuperclasses
    else -> unexpectedClassifier(classifier)
  }

/**
 * Returns the supertypes of this [KType].
 */
val KType.supertypes: List<KType>
  get() = supertypesSequence().toList()

/**
 * Returns all the supertypes of this [KType].
 */
val KType.allSupertypes: Collection<KType>
  get() = allSupertypesSequence().toList()

/**
 * Returns `true` if `this` type is the same or is a subtype of [T], `false` otherwise.
 */
inline fun <reified T> KType.isSubtypeOf(): Boolean = isSubtypeOf(typeOf<T>())

/**
 * Returns `true` if `this` type is the same or is a supertype of [T], `false` otherwise.
 */
inline fun <reified T> KType.isSupertypeOf(): Boolean = isSupertypeOf(typeOf<T>())

/**
 * Resolves the given type parameter from this type.
 */
fun KType.resolve(parameter: KTypeParameter): KType? =
  (listOf(this) + allSupertypesSequence())
    .firstNotNullOfOrNull { type ->
      val index = (type.classifier as KClass<*>).typeParameters.indexOf(parameter)
      if (index == -1) {
        null
      } else {
        type.arguments[index].type
      }
    }

internal fun KType.allSupertypesSequence(): Sequence<KType> =
  supertypesSequence()
    .flatMap { type -> sequenceOf(type) + type.allSupertypesSequence() }
    .distinct()

internal fun KType.supertypesSequence(): Sequence<KType> {
  return when (val classifier = classifier) {
    null, is KTypeParameter -> emptySequence()
    is KClass<*> -> classifier.supertypes.asSequence()
      .map { it.resolveSupertype(classifier, arguments) }
    else -> unexpectedClassifier(classifier)
  }
}

private fun KType.resolveSupertype(
  subClass: KClass<*>,
  subArguments: List<KTypeProjection>
): KType {
  val subArgumentsByTypeParameter = subClass.typeParameters
    .withIndex()
    .associate { (index, typeParameter) -> typeParameter to subArguments[index] }
  return resolveTypeParameters(subArgumentsByTypeParameter)
}

private fun KType.resolveTypeParameters(
  typeProjections: Map<KTypeParameter, KTypeProjection>
): KType {
  val classifier = classifier
  if (classifier == null || arguments.isEmpty())
    return this
  val resolvedArguments = ArrayList<KTypeProjection>(arguments.size)
  var modified = false
  for (argument in arguments) {
    val argumentType = argument.type
    val argumentClassifier = argumentType?.classifier
    val resolved = if (argumentClassifier is KTypeParameter) {
      typeProjections[argumentClassifier]
    } else {
      val type = argument.type
      if (type != null) {
        val resolved = type.resolveTypeParameters(typeProjections)
        if (resolved !== type) {
          KTypeProjection(argument.variance, resolved)
        } else {
          null
        }
      } else {
        null
      }
    }
    if (resolved != null) {
      modified = true
      resolvedArguments.add(resolved)
    } else {
      resolvedArguments.add(argument)
    }
  }
  if (modified)
    return classifier.createType(resolvedArguments, isMarkedNullable, annotations)
  return this
}

internal fun KClassifier.createStarType(
  nullable: Boolean = false,
  annotations: List<Annotation> = emptyList()
): KType {
  return when(this) {
    is KClass<*> -> createStarType(nullable, annotations)
    is KTypeParameter -> createType(emptyList(), nullable, annotations)
    else -> unexpectedClassifier(this)
  }
}

internal fun KClass<*>.createStarType(
  nullable: Boolean = false,
  annotations: List<Annotation> = emptyList()
): KType {
  val projections = ArrayList<KTypeProjection>()
  java.collectTypeProjections(projections)
  return createType(projections, nullable, annotations)
}

internal fun KClass<*>.createParameterizedType(
  nullable: Boolean = false,
  annotations: List<Annotation> = emptyList()
): KType {
  val projections = typeParameters
    .map { parameter -> KTypeProjection.invariant(parameter.createType()) }
  return createType(projections, nullable, annotations)
}

internal fun unexpectedClassifier(classifier: KClassifier): Nothing =
  error("Unexpected classifier: $classifier")
