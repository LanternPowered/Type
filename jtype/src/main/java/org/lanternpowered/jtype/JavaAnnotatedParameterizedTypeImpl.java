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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class JavaAnnotatedParameterizedTypeImpl extends JavaAnnotatedTypeImpl implements AnnotatedParameterizedType {

  private final AnnotatedType[] typeArguments;

  JavaAnnotatedParameterizedTypeImpl(
      Type type,
      Annotation[] annotations,
      AnnotatedType[] typeArguments,
      @Nullable AnnotatedType ownerType,
      @Nullable JType jType) {
    super(type, annotations, ownerType, jType);
    this.typeArguments = typeArguments;
  }

  @Override
  public AnnotatedType[] getAnnotatedActualTypeArguments() {
    return Arrays.copyOf(typeArguments, typeArguments.length);
  }

  private static AnnotatedType[] typeArguments(AnnotatedParameterizedType type) {
    return type instanceof JavaAnnotatedParameterizedTypeImpl ?
        ((JavaAnnotatedParameterizedTypeImpl) type).typeArguments : type.getAnnotatedActualTypeArguments();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof AnnotatedParameterizedType) {
      var parameterizedType = (AnnotatedParameterizedType) obj;
      return typeAndAnnotationEquals(parameterizedType) &&
          Arrays.equals(typeArguments, typeArguments(parameterizedType)) &&
          Objects.equals(ownerType, parameterizedType.getAnnotatedOwnerType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), Arrays.hashCode(typeArguments));
  }

  @Override
  public String toString() {
    var type = rawTypeName();
    if (annotations.length == 0 && typeArguments.length == 0) {
      return type;
    }
    var builder = new StringBuilder();
    for (var annotation : annotations) {
      builder.append(annotation);
      builder.append(' ');
    }
    builder.append(type);
    if (typeArguments.length > 0) {
      builder.append('<');
      for (int i = 0; i < typeArguments.length; i++) {
        if (i != 0) {
          builder.append(", ");
        }
        builder.append(typeArguments[i]);
      }
      builder.append('>');
    }
    return builder.toString();
  }
}
