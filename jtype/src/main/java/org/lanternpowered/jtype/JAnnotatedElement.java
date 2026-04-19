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
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public interface JAnnotatedElement {

  List<Annotation> annotations();

  default <A extends Annotation> List<A> findAnnotations(JClass<A> type) {
    return findAnnotations(type.javaClass());
  }

  default <A extends Annotation> List<A> findAnnotations(Class<A> type) {
    return (List<A>) annotations().stream().filter(type::isInstance).collect(Collectors.toList());
  }

  default <A extends Annotation> @Nullable A findAnnotation(JClass<A> type) {
    return findAnnotation(type.javaClass());
  }

  default <A extends Annotation> @Nullable A findAnnotation(Class<A> type) {
    for (var annotation : annotations()) {
      if (type.isInstance(annotation)) {
        return type.cast(annotation);
      }
    }
    return null;
  }
}
