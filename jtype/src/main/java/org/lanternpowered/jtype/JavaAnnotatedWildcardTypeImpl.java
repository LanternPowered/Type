/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class JavaAnnotatedWildcardTypeImpl extends JavaAnnotatedTypeImpl implements AnnotatedWildcardType {

  private final AnnotatedType[] lowerBounds;
  private final AnnotatedType[] upperBounds;

  JavaAnnotatedWildcardTypeImpl(
      Type type,
      Annotation[] annotations,
      AnnotatedType[] lowerBounds,
      AnnotatedType[] upperBounds,
      @Nullable JType jType) {
    super(type, annotations, null, jType);
    this.lowerBounds = lowerBounds;
    this.upperBounds = upperBounds;
  }

  @Override
  public AnnotatedType[] getAnnotatedLowerBounds() {
    return lowerBounds;
  }

  @Override
  public AnnotatedType[] getAnnotatedUpperBounds() {
    return upperBounds;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof AnnotatedWildcardType) {
      var wildcardType = (AnnotatedWildcardType) obj;
      if (!typeAndAnnotationEquals(wildcardType)) {
        return false;
      }
      if (wildcardType instanceof JavaAnnotatedWildcardTypeImpl) {
        var impl = (JavaAnnotatedWildcardTypeImpl) wildcardType;
        return Arrays.equals(upperBounds, impl.upperBounds) &&
            Arrays.equals(lowerBounds, impl.lowerBounds);
      }
      return Arrays.equals(lowerBounds, wildcardType.getAnnotatedLowerBounds()) &&
          Arrays.equals(upperBounds, wildcardType.getAnnotatedUpperBounds());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), Arrays.hashCode(lowerBounds), Arrays.hashCode(upperBounds));
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    for (var annotation : annotations) {
      builder.append(annotation);
      builder.append(' ');
    }
    builder.append('?');
    var bounds = lowerBounds;
    if (bounds.length > 0) {
      builder.append(" super ");
    } else {
      bounds = upperBounds;
      if (bounds.length == 0) {
        return builder.toString();
      }
      if (bounds.length == 1) {
        var bound = bounds[0];
        if (bound.getType().equals(Object.class) && bound.getAnnotations().length == 0) {
          return builder.toString();
        }
      }
      builder.append(" extends ");
    }
    for (int i = 0; i < bounds.length; i++) {
      if (i != 0) {
        builder.append(" & ");
      }
      builder.append(bounds[i]);
    }
    return builder.toString();
  }
}
