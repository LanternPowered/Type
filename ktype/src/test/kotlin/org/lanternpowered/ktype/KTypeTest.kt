/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("ClassName")

package org.lanternpowered.ktype

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class KTypeTest {

  interface ASuper<X>
  interface A<X> : ASuper<X>
  interface BSuper<Y>
  interface B<Y> : BSuper<Y>
  interface AB<X, Y> : A<X>, B<Y>
  interface BA<Y, X> : AB<X, Y>
  interface CA<Z, X> : A<X>
  interface AList<X> : A<List<X>>

  interface AInt_B<Y> : A<Int>, B<Y>

  @Test
  fun testA() {
    val type = typeOf<A<Int>>()
    val supertype = type.supertypes.first()
    assertEquals(typeOf<ASuper<Int>>(), supertype)
  }

  @Test
  fun testAInt_B() {
    val type = typeOf<AInt_B<Double>>()
    val supertypes = type.supertypes
    assertTrue(typeOf<A<Int>>() in supertypes)
    assertTrue(typeOf<B<Double>>() in supertypes)
  }

  @Test
  fun testASuperType() {
    val type = typeOf<A<Int>>()
    assertTrue(type.isSubtypeOf<ASuper<*>>())
    assertTrue(type.isSubtypeOf<ASuper<out Number>>())
    assertTrue(type.isSubtypeOf<ASuper<Int>>())
    assertFalse(type.isSubtypeOf<ASuper<Double>>())
    assertFalse(type.isSubtypeOf<ASuper<out Collection<*>>>())
  }

  @Test
  fun testAB() {
    val ba = typeOf<BA<Int, String>>()
    val ab = ba.supertypes.first()
    assertEquals(typeOf<AB<String, Int>>(), ab)
  }

  @Test
  fun testSimpleName() {
    assertEquals("Fruit", typeOf<Fruit>().toSimpleString())
    assertEquals("Outer<*>.Inner<*>", typeOf<Outer<*>.Inner<*>>().toSimpleString())
    assertEquals("Outer<*>.Inner<Fruit>", typeOf<Outer<*>.Inner<Fruit>>().toSimpleString())
  }

  @Test
  fun testResolveTypeParameter() {
    val type = typeOf<A<Int>>()
    assertEquals(typeOf<Int>(), type.resolve(A::class.typeParameters[0]))
    assertEquals(typeOf<Int>(), type.resolve(ASuper::class.typeParameters[0]))
    val type2 = typeOf<CA<Int, String>>()
    assertEquals(typeOf<String>(), type2.resolve(A::class.typeParameters[0]))
    val type3 = typeOf<AList<Int>>()
    assertEquals(typeOf<List<Int>>(), type3.resolve(A::class.typeParameters[0]))
    assertEquals(typeOf<Int>(), type3.resolve(AList::class.typeParameters[0]))
  }
}
