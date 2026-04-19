/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.TypeVariable;
import java.util.List;

final class JTypeParameterImpl implements JTypeParameter {

  private final TypeVariable<?> typeVariable;
  private @Nullable JType upperBound;
  private @Nullable JType unresolvedType;
  private @Nullable JType starType;

  JTypeParameterImpl(TypeVariable<?> typeVariable) {
    this.typeVariable = typeVariable;
  }

  public TypeVariable<?> typeVariable() {
    return typeVariable;
  }

  @Override
  public String name() {
    return typeVariable.getName();
  }

  @Override
  public JType upperBound() {
    var upperBound = this.upperBound;
    if (upperBound == null) {
      upperBound = JTypeImpl.boundsToType(typeVariable.getAnnotatedBounds());
      if (upperBound == null) {
        upperBound = JTypeImpl.OBJECT;
      }
      this.upperBound = upperBound;
    }
    return upperBound;
  }

  @Override
  public JType unresolvedType() {
    var unresolvedType = this.unresolvedType;
    if (unresolvedType == null) {
      unresolvedType = new JTypeImpl(this, List.of(), false, List.of());
      this.unresolvedType = unresolvedType;
    }
    return unresolvedType;
  }

  @Override
  public JType starType() {
    var starType = this.starType;
//    if (starType == null) {
//      starType = upperBounds().get(0);
//      this.starType = starType;
//    }
    return starType;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JTypeParameterImpl && ((JTypeParameterImpl) obj).typeVariable.equals(typeVariable);
  }

  @Override
  public int hashCode() {
    return typeVariable.hashCode();
  }

  @Override
  public String toString() {
    return name();
  }
}
