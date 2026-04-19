/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class AnnotatedJavaTypes {

  public static AnnotatedType of(Type type) {
    return JavaAnnotatedTypeImpl.of(requireNonNull(type, "type"));
  }

  public static AnnotatedType of(Type type, List<Annotation> annotations) {
    return JavaAnnotatedTypeImpl.of(requireNonNull(type, "type"), annotations);
  }

  private AnnotatedJavaTypes() {
  }
}
