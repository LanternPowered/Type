/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

public interface GenericType<T extends @Nullable Object> extends JType {

  @NullUnmarked
  static <T> @NonNull GenericType<T> of(@NonNull Class<? super @NonNull T> type) {
    return JTypeImpl.genericOf(requireNonNull(type, "type"));
  }

  @SuppressWarnings("unchecked")
  static <T> GenericType<T> nonNullOf(Class<? super T> type) {
    return (GenericType<T>) of(type).withNullability(Nullability.NON_NULL);
  }

  @SuppressWarnings("unchecked")
  static <T> GenericType<@Nullable T> nullableOf(Class<? super T> type) {
    return (GenericType<T>) of(type).withNullability(Nullability.NULLABLE);
  }

  @NullUnmarked
  static <T> @NonNull GenericType<T> of(@NonNull Type type) {
    return JTypeImpl.genericOf(requireNonNull(type, "type"));
  }

  @SuppressWarnings("unchecked")
  static <T> GenericType<T> nonNullOf(Type type) {
    return (GenericType<T>) of(type).withNullability(Nullability.NON_NULL);
  }

  @SuppressWarnings("unchecked")
  static <T> GenericType<@Nullable T> nullableOf(Type type) {
    return (GenericType<T>) of(type).withNullability(Nullability.NULLABLE);
  }

  @NullUnmarked
  static <T> @NonNull GenericType<T> of(@NonNull AnnotatedType type) {
    return JTypeImpl.genericOf(requireNonNull(type, "type"));
  }

  @SuppressWarnings("unchecked")
  static <T> GenericType<T> nonNullOf(AnnotatedType type) {
    return (GenericType<T>) of(type).withNullability(Nullability.NON_NULL);
  }

  @SuppressWarnings("unchecked")
  static <T> GenericType<@Nullable T> nullableOf(AnnotatedType type) {
    return (GenericType<T>) of(type).withNullability(Nullability.NULLABLE);
  }

  @Override
  JClass<? super @NonNull T> rawType();

  @NullUnmarked
  @Override
  @NonNull GenericType<T> withNullability(@NonNull Nullability nullability);
}
