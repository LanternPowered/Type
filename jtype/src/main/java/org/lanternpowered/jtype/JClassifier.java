/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.annotation.Annotation;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface JClassifier {

  /**
   * Returns a {@link JType} where all the type parameters are unresolved.
   */
  JType unresolvedType();

  /**
   * Returns a {@link JType} where all the type parameters are wildcards.
   */
  JType starType();

  default JType createType(List<JTypeProjection> arguments) {
    return createType(arguments, Nullability.NON_NULL, List.of());
  }

  default JType createType(List<JTypeProjection> arguments, Nullability nullability) {
    return createType(arguments, nullability, List.of());
  }

  default JType createType(List<JTypeProjection> arguments, List<Annotation> annotations) {
    return createType(arguments, Nullability.NON_NULL, annotations);
  }

  default JType createType(List<JTypeProjection> arguments, Nullability nullability, List<Annotation> annotations) {
    requireNonNull(arguments, "arguments");
    requireNonNull(annotations, "annotations");
    return new JTypeImpl(this, List.copyOf(arguments), nullability, List.copyOf(annotations));
  }
}
