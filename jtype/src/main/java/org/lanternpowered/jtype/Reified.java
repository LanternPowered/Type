/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents a value with a specific reified {@link JType} that cannot be derived from the value class.
 */
public final class Reified<T extends @Nullable Object> {

  /**
   * Returns a {@link Reified} based on the target {@code value} and reified {@code type}. The reified type
   * will be generated from both the instance type and the reified type information.
   * <p>
   * For example, when reifying an {@code ArrayList} instance which is stored in a parameter with the type
   * {@code List<Integer>}. The returned reified type will be {@code ArrayList<Integer>}.
   */
  public static <T extends @Nullable Object> Reified<T> reify(T value, Type type) {
    return reify(value, JType.of(type));
  }

  /**
   * Returns a {@link Reified} based on the target {@code value} and reified {@code type}. The reified type
   * will be generated from both the instance type and the reified type information.
   * <p>
   * For example, when reifying an {@code ArrayList} instance which is stored in a parameter with the type
   * {@code List<Integer>}. The returned reified type will be {@code ArrayList<Integer>}.
   */
  public static <T extends @Nullable Object> Reified<T> reify(T value, AnnotatedType type) {
    return reify(value, JType.of(type));
  }

  /**
   * Returns a {@link Reified} based on the target {@code value} and reified {@code type}. The reified type
   * will be generated from both the instance type and the reified type information.
   * <p>
   * For example, when reifying an {@code ArrayList} instance which is stored in a parameter with the type
   * {@code List<Integer>}. The returned reified type will be {@code ArrayList<Integer>}.
   */
  public static <T extends @Nullable Object> Reified<T> reify(T value, JType type) {
    requireNonNull(type, "type");
    if (value == null) {
      return new Reified<>(null, type);
    }
    var instanceType = JClass.of(value.getClass()).starType();
    return new Reified<>(value, instanceType.reify(type));
  }

  /**
   * Returns a {@link Reified} with the given value and type.
   */
  public static <T extends @Nullable Object> Reified<T> of(T value, JType type) {
    requireNonNull(type, "type");
    return new Reified<>(value, type);
  }

  private final T value;
  private final JType type;

  private Reified(T value, JType type) {
    this.value = value;
    this.type = type;
  }

  public JType type() {
    return type;
  }

  public T value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Reified)) {
      return false;
    }
    var that = (Reified<?>) o;
    return Objects.equals(value, that.value) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, type);
  }

  @Override
  public String toString() {
    return "Reified{" +
        "value=" + value +
        ", type=" + type +
        '}';
  }
}
