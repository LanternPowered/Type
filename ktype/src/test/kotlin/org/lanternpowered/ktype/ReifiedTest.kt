/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.ktype

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReifiedTest {

  @Test
  fun testSimple() {
    val apples: Collection<Apple> = ArrayList()
    val reified = apples.reify()
    assertEquals(ArrayList::class, reified.type.classifier)
    assertTrue(reified.type.isSubtypeOf<ArrayList<Apple>>())
    assertTrue(reified.type.isSubtypeOf<Collection<Apple>>())
    assertTrue(reified.type.isSubtypeOf<Collection<*>>())
    assertFalse(reified.type.isSubtypeOf<Collection<Banana>>())
  }
}
