/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

final class JavaWildcardTypeImpl extends JavaTypeImpl implements WildcardType {

  private final Type[] upperBounds;
  private final Type[] lowerBounds;

  JavaWildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
    this.upperBounds = upperBounds;
    this.lowerBounds = lowerBounds;
  }

  @Override
  public Type[] getUpperBounds() {
    return Arrays.copyOf(upperBounds, upperBounds.length);
  }

  @Override
  public Type[] getLowerBounds() {
    return Arrays.copyOf(lowerBounds, lowerBounds.length);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof WildcardType) {
      var wildcardType = (WildcardType) obj;
      if (wildcardType instanceof JavaWildcardTypeImpl) {
        var impl = (JavaWildcardTypeImpl) wildcardType;
        return Arrays.equals(upperBounds, impl.upperBounds) &&
            Arrays.equals(lowerBounds, impl.lowerBounds);
      }
      return Arrays.equals(upperBounds, wildcardType.getUpperBounds()) &&
          Arrays.equals(lowerBounds, wildcardType.getLowerBounds());
    }
    return super.equals(obj);
  }
}
