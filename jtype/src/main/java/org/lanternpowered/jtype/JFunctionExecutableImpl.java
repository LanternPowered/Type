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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class JFunctionExecutableImpl<R> implements JFunction<R> {

  static <R> JFunctionExecutableImpl<R> of(Executable executable) {
    return JClassImpl.of(executable.getDeclaringClass()).function(executable);
  }

  final Executable executable;
  private final List<JParameter> parameters;
  private final JType returnType;
  private final List<JTypeParameter> typeParameters;
  private final List<Annotation> annotations;

  JFunctionExecutableImpl(Executable executable) {
    this.executable = executable;
    var originalTypeParameters = executable.getTypeParameters();
    var typeParameters = new ArrayList<JTypeParameter>(originalTypeParameters.length);
    for (var typeParameter : originalTypeParameters) {
      typeParameters.add(new JTypeParameterImpl(typeParameter));
    }
    this.typeParameters = List.copyOf(typeParameters);
    this.annotations = List.of(executable.getAnnotations());
    var context = new JTypeContext();
    context.function = this;
    context.nullMarked = JTypeContext.nullMarked(executable);
    var originalParameters = executable.getParameters();
    var parameters = new ArrayList<JParameter>(originalParameters.length);
    for (int i = 0; i < originalParameters.length; i++) {
      parameters.add(new JParameterImpl(i, originalParameters[i].getName(),
          JTypeImpl.of(originalParameters[i].getAnnotatedType(), context), executable));
    }
    this.parameters = List.copyOf(parameters);
    this.returnType = JTypeImpl.of(executable.getAnnotatedReturnType(), context);
  }

  @Override
  public String name() {
    return executable.getName();
  }

  @Override
  public List<JParameter> parameters() {
    return parameters;
  }

  @Override
  public JType returnType() {
    return returnType;
  }

  @Override
  public List<JTypeParameter> typeParameters() {
    return typeParameters;
  }

  @SuppressWarnings("unchecked")
  @Override
  public R call(@Nullable Object... args) {
    requireNonNull(args, "args");
    try {
      if (executable instanceof Method) {
        Object receiver = null;
        if (!Modifier.isStatic(executable.getModifiers())) {
          receiver = args[0];
          args = Arrays.copyOfRange(args, 1, args.length - 1);
        }
        return (R) ((Method) executable).invoke(receiver, args);
      } else {
        return ((Constructor<R>) executable).newInstance(args);
      }
    } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public List<Annotation> annotations() {
    return annotations;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JFunctionExecutableImpl &&
        ((JFunctionExecutableImpl<?>) obj).executable.equals(executable);
  }

  @Override
  public int hashCode() {
    return executable.hashCode();
  }

  @Override
  public String toString() {
    return executable.toString(); // TODO
  }
}
