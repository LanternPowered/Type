/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.ktype

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.typeOf

class GenericTypeTest {

  // TODO: It's currently not possible to create a reified function for the TypeCapture,
  //   see https://youtrack.jetbrains.com/issue/KT-17103

  @Test
  fun testIntToKType() {
    val type = object : TypeCapture<Int>() {}.toKType()
    assertEquals(Int::class, type.classifier)
    assertEquals(false, type.isMarkedNullable)
    assertIterableEquals(emptyList<KTypeProjection>(), type.arguments)
    assertIterableEquals(emptyList<Annotation>(), type.annotations)
  }

  @Test
  fun testNullableIntToKType() {
    val type = object : TypeCapture<Int?>() {}.toKType()
    assertEquals(Int::class, type.classifier)
    assertEquals(true, type.isMarkedNullable)
  }

  @Test
  fun testJavaNullableIntToKType() {
    val type = JavaTypes.NullableInt.toKType()
    assertEquals(Int::class, type.classifier)
    assertEquals(true, type.isMarkedNullable)
  }

  @Test
  fun testInvariantToKType() {
    val type = JavaTypes.Fruits.toKType()
    assertEquals(Collection::class, type.classifier)
    assertEquals(false, type.isMarkedNullable)
    val elementType = type.arguments.first()
    assertEquals(KVariance.INVARIANT, elementType.variance)
    assertEquals(Fruit::class, elementType.type?.classifier)
  }

  @Test
  fun testCovariantToKType() {
    val type = JavaTypes.CovariantFruits.toKType()
    assertEquals(Collection::class, type.classifier)
    assertEquals(false, type.isMarkedNullable)
    val elementType = type.arguments.first()
    assertEquals(KVariance.OUT, elementType.variance)
    assertEquals(Fruit::class, elementType.type?.classifier)
  }

  @Test
  fun testContravariantToKType() {
    val type = JavaTypes.ContravariantFruits.toKType()
    assertEquals(Collection::class, type.classifier)
    assertEquals(false, type.isMarkedNullable)
    val elementType = type.arguments.first()
    assertEquals(KVariance.IN, elementType.variance)
    assertEquals(Fruit::class, elementType.type?.classifier)
  }

  @Test
  fun testVoidToUnit() {
    var type = JavaTypes.Void.toKType()
    assertEquals(Unit::class, type.classifier)
    type = Void.TYPE.toKType()
    assertEquals(Unit::class, type.classifier)
  }

  @Test
  fun testOuterAppleInnerBanana() {
    fun assertType(type: KType) {
      assertEquals(Outer.Inner::class, type.classifier)
      val arguments = type.arguments
      assertEquals(2, arguments.size)
      assertEquals(KVariance.INVARIANT, arguments[0].variance)
      assertEquals(Banana::class, arguments[0].type?.classifier)
      assertEquals(KVariance.INVARIANT, arguments[1].variance)
      assertEquals(Apple::class, arguments[1].type?.classifier)
    }
    assertType(JavaTypes.OuterAppleInnerBanana.toKType())
    assertType(typeOf<Outer<Apple>.Inner<Banana>>())
  }

  @Test
  fun testTFruit() {
    val type = JavaTypes.TFruit.toKType()
    val classifier = type.classifier
    assertTrue(classifier is KTypeParameter)
    classifier as KTypeParameter
    assertEquals("T", classifier.name)
    assertEquals(KVariance.INVARIANT, classifier.variance)
    assertEquals(Fruit::class, classifier.upperBounds.first().classifier)
  }
}
