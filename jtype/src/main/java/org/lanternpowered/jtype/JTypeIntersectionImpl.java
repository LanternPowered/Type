/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.util.List;

final class JTypeIntersectionImpl implements JTypeIntersection {

  private final List<JType> types;
  private @Nullable JType unresolvedType;

  JTypeIntersectionImpl(List<JType> types) {
    this.types = types;
  }

  @Override
  public List<JType> types() {
    return types;
  }

  @Override
  public JType unresolvedType() {
    var unresolvedType = this.unresolvedType;
    if (unresolvedType == null) {
      var nullability = JTypeImpl.nullabilityOfTypes(types);
      unresolvedType = new JTypeImpl(this, List.of(), nullability, List.of());
      this.unresolvedType = unresolvedType;
    }
    return unresolvedType;
  }

  @Override
  public JType starType() {
    return unresolvedType();
  }
}
