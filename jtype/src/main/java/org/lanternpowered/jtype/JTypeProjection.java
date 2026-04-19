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

public final class JTypeProjection {

  private static final JTypeProjection STAR = new JTypeProjection(null, null);

  public static JTypeProjection star() {
    return STAR;
  }

  public static JTypeProjection invariant(AnnotatedType type) {
    return invariant(JType.of(type));
  }

  public static JTypeProjection invariant(Type type) {
    return invariant(JType.of(type));
  }

  public static JTypeProjection invariant(JType type) {
    return new JTypeProjection(JVariance.INVARIANT, type);
  }

  public static JTypeProjection covariant(AnnotatedType type) {
    return covariant(JType.of(type));
  }

  public static JTypeProjection covariant(Type type) {
    return covariant(JType.of(type));
  }

  public static JTypeProjection covariant(JType type) {
    return new JTypeProjection(JVariance.OUT, type);
  }

  public static JTypeProjection contravariant(AnnotatedType type) {
    return contravariant(JType.of(type));
  }

  public static JTypeProjection contravariant(Type type) {
    return contravariant(JType.of(type));
  }

  public static JTypeProjection contravariant(JType type) {
    return new JTypeProjection(JVariance.IN, type);
  }

  public static JTypeProjection of(@Nullable JVariance variance, @Nullable AnnotatedType type) {
    return of(variance, type == null ? null : JType.of(type));
  }

  public static JTypeProjection of(@Nullable JVariance variance, @Nullable Type type) {
    return of(variance, type == null ? null : JType.of(type));
  }

  public static JTypeProjection of(@Nullable JVariance variance, @Nullable JType type) {
    if ((type == null) != (variance == null)) {
      if (variance == null) {
        throw new IllegalArgumentException("Star projection must have no type specified.");
      } else {
        throw new IllegalArgumentException("The projection variance " + variance + " requires type to be specified.");
      }
    }
    return new JTypeProjection(variance, type);
  }

  private final @Nullable JVariance variance;
  private final @Nullable JType type;

  private JTypeProjection(@Nullable JVariance variance, @Nullable JType type) {
    this.variance = variance;
    this.type = type;
  }

  public @Nullable JType type() {
    return type;
  }

  public @Nullable JVariance variance() {
    return variance;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof JTypeProjection) {
      JTypeProjection that = (JTypeProjection) o;
      return variance == that.variance && Objects.equals(type, that.type);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(variance, type);
  }

  @Override
  public String toString() {
    if (variance == null) {
      return "?";
    } else if (variance == JVariance.INVARIANT) {
      return Objects.toString(type);
    } else if (variance == JVariance.IN) {
      return "? super " + type;
    } else if (variance == JVariance.OUT) {
      return "? extends " + type;
    } else {
      throw new IllegalStateException("Unexpected variance: " + variance);
    }
  }
}
