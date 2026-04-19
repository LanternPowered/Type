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
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class JavaAnnotatedTypeVariableImpl extends JavaAnnotatedTypeImpl implements AnnotatedTypeVariable {

  private final AnnotatedType[] bounds;

  JavaAnnotatedTypeVariableImpl(Type type, Annotation[] annotations, AnnotatedType[] bounds, @Nullable JType jType) {
    super(type, annotations, null, jType);
    this.bounds = bounds;
  }

  @Override
  public AnnotatedType[] getAnnotatedBounds() {
    return bounds;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof AnnotatedTypeVariable) {
      var typeVariable = (AnnotatedTypeVariable) obj;
      return typeAndAnnotationEquals(typeVariable) &&
          Arrays.equals(bounds, typeVariable.getAnnotatedBounds());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), Arrays.hashCode(bounds));
  }
}
