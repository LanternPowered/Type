/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test;

import org.junit.jupiter.api.Test;
import org.lanternpowered.jtype.Nullability;
import org.lanternpowered.jtype.test.nullunmarked.CheckerFrameworkTypes;
import org.lanternpowered.jtype.test.nullunmarked.JSpecifyTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class NullabilityTest {

  @Test
  public void jspecify() {
    assertEquals(Nullability.UNKNOWN, JSpecifyTypes.unknownApple.nullability());
    assertEquals(Nullability.NULLABLE, JSpecifyTypes.nullableApple.nullability());
    assertEquals(Nullability.NON_NULL, JSpecifyTypes.nonNullApple.nullability());
    assertEquals(Nullability.NULLABLE, JSpecifyTypes.DefaultNonNull.nullableApple.nullability());
    assertEquals(Nullability.NON_NULL, JSpecifyTypes.DefaultNonNull.nonNullApple.nullability());
  }

  @Test
  public void checkerFramework() {
    assertEquals(Nullability.UNKNOWN, CheckerFrameworkTypes.unknownApple.nullability());
    assertEquals(Nullability.NULLABLE, CheckerFrameworkTypes.nullableApple.nullability());
    assertEquals(Nullability.NON_NULL, CheckerFrameworkTypes.nonNullApple.nullability());
    assertEquals(Nullability.NULLABLE, CheckerFrameworkTypes.DefaultNullable.nullableApple.nullability());
    assertEquals(Nullability.NON_NULL, CheckerFrameworkTypes.DefaultNullable.nonNullApple.nullability());
    assertEquals(Nullability.NULLABLE, CheckerFrameworkTypes.DefaultNonNull.nullableApple.nullability());
    assertEquals(Nullability.NON_NULL, CheckerFrameworkTypes.DefaultNonNull.nonNullApple.nullability());
  }
}
