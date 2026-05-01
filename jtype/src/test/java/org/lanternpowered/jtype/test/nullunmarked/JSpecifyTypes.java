/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test.nullunmarked;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lanternpowered.jtype.JType;
import org.lanternpowered.jtype.JTypeCapture;
import org.lanternpowered.jtype.test.Apple;

public final class JSpecifyTypes {
  public static final JType unknownApple = new JTypeCapture<Apple>() {};
  public static final JType nullableApple = new JTypeCapture<@Nullable Apple>() {};
  public static final JType nonNullApple = new JTypeCapture<@NonNull Apple>() {};

  @NullMarked
  public static final class DefaultNonNull {
    public static final JType nullableApple = new JTypeCapture<@Nullable Apple>() {};
    public static final JType nonNullApple = new JTypeCapture<Apple>() {};
  }
}
