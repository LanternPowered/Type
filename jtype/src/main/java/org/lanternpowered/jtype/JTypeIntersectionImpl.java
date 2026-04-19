/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.util.List;

final class JTypeIntersectionImpl implements JTypeIntersection {

  private final List<JType> types;

  JTypeIntersectionImpl(List<JType> types) {
    this.types = types;
  }

  @Override
  public List<JType> types() {
    return types;
  }

  @Override
  public JType unresolvedType() {
    var nullability = JTypeImpl.nullabilityOfTypes(types);
    return new JTypeImpl(this, List.of(), nullability, List.of());
  }

  @Override
  public JType starType() {
    return unresolvedType();
  }
}
