/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.ktype;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

public final class JavaTypes {

  public static final TypeCapture<@Nullable Integer> NullableInt =
    new TypeCapture<@Nullable Integer>() {};

  public static final TypeCapture<Collection<Fruit>> Fruits =
    new TypeCapture<>() {};

  public static final TypeCapture<Collection<? extends Fruit>> CovariantFruits =
    new TypeCapture<>() {};

  public static final TypeCapture<Collection<? super Fruit>> ContravariantFruits =
    new TypeCapture<>() {};

  public static final TypeCapture<Void> Void =
    new TypeCapture<>() {};

  public static final TypeCapture<Outer<Apple>.Inner<Banana>> OuterAppleInnerBanana =
    new TypeCapture<>() {};

  public static final TypeCapture<?> TFruit = createGenericTFruit();

  private static <T extends Fruit> TypeCapture<T> createGenericTFruit() {
    return new TypeCapture<>() {};
  }

  private JavaTypes() {
  }
}
