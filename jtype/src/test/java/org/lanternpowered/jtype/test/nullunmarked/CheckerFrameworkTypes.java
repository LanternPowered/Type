/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test.nullunmarked;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.lanternpowered.jtype.JType;
import org.lanternpowered.jtype.JTypeCapture;
import org.lanternpowered.jtype.test.Apple;

public final class CheckerFrameworkTypes {
  public static final JType unknownApple = new JTypeCapture<Apple>() {};
  public static final JType nullableApple = new JTypeCapture<@Nullable Apple>() {};
  public static final JType nonNullApple = new JTypeCapture<@NonNull Apple>() {};

  @DefaultQualifier(Nullable.class)
  public static final class DefaultNullable {
    public static final JType nullableApple = new JTypeCapture<Apple>() {};
    public static final JType nonNullApple = new JTypeCapture<@NonNull Apple>() {};
  }

  @DefaultQualifier(NonNull.class)
  public static final class DefaultNonNull {
    public static final JType nullableApple = new JTypeCapture<@Nullable Apple>() {};
    public static final JType nonNullApple = new JTypeCapture<Apple>() {};
  }
}
