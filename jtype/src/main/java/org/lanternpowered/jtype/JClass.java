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

import static java.util.Objects.requireNonNull;

public interface JClass<T> extends JClassifier, JAnnotatedElement {

  static <T> JClass<T> of(Class<T> type) {
    return JClassImpl.of(requireNonNull(type, "type"));
  }

  Class<T> javaClass();

  String name();

  String qualifiedName();

  List<JTypeParameter> typeParameters();

  boolean isSuperclassOf(Class<?> derived);

  boolean isSuperclassOf(JClass<?> derived);

  boolean isSubclassOf(Class<?> base);

  boolean isSubclassOf(JClass<?> base);

  List<JClass<?>> superclasses();

  List<JClass<?>> allSuperclasses();

  List<JType> supertypes();

  List<JType> allSupertypes();

  boolean isInstance(@Nullable Object value);

  boolean isFinal();

  boolean isAbstract();

  boolean isInterface();

  boolean isArray();

  boolean isPrimitive();

  /**
   * Returns this class as its boxed type, or itself if already boxed.
   */
  JClass<?> boxed();

  @Nullable JClass<?> owner();
}
