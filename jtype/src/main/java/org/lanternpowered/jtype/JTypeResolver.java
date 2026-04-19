/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public interface JTypeResolver {

  static JTypeResolver create() {
    return new JTypeResolverImpl();
  }

  default JTypeResolver where(TypeVariable<?> typeVariable, Type type) {
    return where(typeVariable, JType.of(type));
  }

  default JTypeResolver where(TypeVariable<?> typeVariable, AnnotatedType type) {
    return where(typeVariable, JType.of(type));
  }

  default JTypeResolver where(TypeVariable<?> typeVariable, JType type) {
    return where(JTypeParameter.of(typeVariable), type);
  }

  default JTypeResolver where(JTypeCapture<?> typeParameter, Type type) {
    return where(typeParameter, JType.of(type));
  }

  default JTypeResolver where(JTypeCapture<?> typeParameter, AnnotatedType type) {
    return where(typeParameter, JType.of(type));
  }

  default JTypeResolver where(JTypeParameter typeParameter, Type type) {
    return where(typeParameter, JType.of(type));
  }

  default JTypeResolver where(JTypeParameter typeParameter, AnnotatedType type) {
    return where(typeParameter, JType.of(type));
  }

  JTypeResolver where(JTypeParameter typeParameter, JType type);

  JTypeResolver where(JTypeCapture<?> typeParameter, JType type);

  JTypeResolver where(JParameter parameter, JType type);

  default JTypeResolver whereReturns(Executable executable, Type type) {
    return whereReturns(JFunction.of(executable), type);
  }

  default JTypeResolver whereReturns(Executable executable, AnnotatedType type) {
    return whereReturns(JFunction.of(executable), type);
  }

  default JTypeResolver whereReturns(Executable executable, JType type) {
    return whereReturns(JFunction.of(executable), type);
  }

  default JTypeResolver whereReturns(JFunction<?> function, Type type) {
    return whereReturns(function, JType.of(type));
  }

  default JTypeResolver whereReturns(JFunction<?> function, AnnotatedType type) {
    return whereReturns(function, JType.of(type));
  }

  JTypeResolver whereReturns(JFunction<?> function, JType type);

  default JTypeResolver whereReceiver(Type type) {
    return whereReceiver(JType.of(type));
  }

  default JTypeResolver whereReceiver(AnnotatedType type) {
    return whereReceiver(JType.of(type));
  }

  JTypeResolver whereReceiver(JType type);

  default JType resolve(Class<?> type) {
    return resolve(JClass.of(type));
  }

  default JType resolve(JClass<?> type) {
    return resolve(type.unresolvedType());
  }

  default JType resolve(JParameter parameter) {
    return resolve(parameter.type());
  }

  default JType resolve(JTypeParameter typeParameter) {
    return resolve(typeParameter.unresolvedType());
  }

  JType resolve(JType type);
}
