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
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Objects;

final class JavaAnnotatedArrayTypeImpl extends JavaAnnotatedTypeImpl implements AnnotatedArrayType {

  private final AnnotatedType componentType;

  JavaAnnotatedArrayTypeImpl(Type type, Annotation[] annotations, AnnotatedType componentType, @Nullable JType jType) {
    super(type, annotations, null, jType);
    this.componentType = componentType;
  }

  @Override
  public AnnotatedType getAnnotatedGenericComponentType() {
    return componentType;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof AnnotatedArrayType) {
      var arrayType = (AnnotatedArrayType) obj;
      return typeAndAnnotationEquals(arrayType) &&
          Objects.equals(componentType, arrayType.getAnnotatedGenericComponentType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), componentType);
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append(componentType);
    for (var annotation : annotations) {
      builder.append(' ');
      builder.append(annotation);
    }
    builder.append("[]");
    return builder.toString();
  }
}
