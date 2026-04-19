/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.reflect.TypeVariable;

import static java.util.Objects.requireNonNull;

/**
 * Represents parameter of a type.
 */
public interface JTypeParameter extends JClassifier {

  /**
   * Returns the type parameter for the given {@link TypeVariable}.
   */
  static JTypeParameter of(TypeVariable<?> typeVariable) {
    return new JTypeParameterImpl(requireNonNull(typeVariable, "typeVariable"));
  }

  /**
   * Returns the name of the type parameter.
   */
  String name();

  /**
   * Returns the upper bound of the type parameter.
   */
  JType upperBound();
}
