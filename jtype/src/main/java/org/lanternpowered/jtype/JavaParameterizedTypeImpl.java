/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class JavaParameterizedTypeImpl extends JavaTypeImpl implements ParameterizedType {

  private final Class<?> type;
  private final Type[] typeArguments;
  private final @Nullable Type ownerType;

  JavaParameterizedTypeImpl(Class<?> type, Type[] typeArguments, @Nullable Type ownerType) {
    this.type = type;
    this.typeArguments = typeArguments;
    this.ownerType = ownerType;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return Arrays.copyOf(typeArguments, typeArguments.length);
  }

  @Override
  public Class<?> getRawType() {
    return type;
  }

  @Override
  public @Nullable Type getOwnerType() {
    return ownerType;
  }

  private static Type[] typeArguments(ParameterizedType type) {
    return type instanceof JavaParameterizedTypeImpl ?
        ((JavaParameterizedTypeImpl) type).typeArguments : type.getActualTypeArguments();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ParameterizedType) {
      var parameterizedType = (ParameterizedType) obj;
      return type.equals(parameterizedType.getRawType()) &&
          Arrays.equals(typeArguments, typeArguments(parameterizedType)) &&
          Objects.equals(ownerType, parameterizedType.getOwnerType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), Arrays.hashCode(typeArguments));
  }

  @Override
  public String toString() {
    var type = this.type.getTypeName();
    if (typeArguments.length == 0) {
      return type;
    }
    var builder = new StringBuilder();
    builder.append(type);
    builder.append('<');
    for (int i = 0; i < typeArguments.length; i++) {
      if (i != 0) {
        builder.append(", ");
      }
      builder.append(typeArguments[0]);
    }
    builder.append('>');
    return builder.toString();
  }
}
