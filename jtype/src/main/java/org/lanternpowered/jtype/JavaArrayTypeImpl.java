/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

final class JavaArrayTypeImpl extends JavaTypeImpl implements GenericArrayType {

  private final Type componentType;

  JavaArrayTypeImpl(Type componentType) {
    this.componentType = componentType;
  }

  @Override
  public Type getGenericComponentType() {
    return componentType;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GenericArrayType && ((GenericArrayType) obj).getGenericComponentType().equals(componentType);
  }

  @Override
  public int hashCode() {
    return componentType.hashCode();
  }

  @Override
  public String toString() {
    return componentType + "[]";
  }
}
