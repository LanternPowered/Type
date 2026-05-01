/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test;

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.lanternpowered.jtype.GenericType;
import org.lanternpowered.jtype.JTypeCapture;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class GenericTypeTest {

  @SuppressWarnings({"rawtypes", "AssertBetweenInconvertibleTypes"})
  @NullUnmarked
  @Test
  public void test() {
    var apple = GenericType.of(Apple.class);
    assertEquals(new JTypeCapture<Apple>() {}, apple);
    GenericType<List<Apple>> appleList = GenericType.of(List.class);
    assertEquals(new JTypeCapture<List>() {}, appleList);
    var nullableApple = GenericType.nullableOf(Apple.class);
    assertEquals(new JTypeCapture<@Nullable Apple>() {}, nullableApple);
  }
}
