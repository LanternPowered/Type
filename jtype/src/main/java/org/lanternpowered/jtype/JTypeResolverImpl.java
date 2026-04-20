/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

final class JTypeResolverImpl implements JTypeResolver {

  // TODO: Resolve type variables in type variable bounds

  private final Map<JTypeParameter, JType> typeParameters = new HashMap<>();
  private @Nullable Executable executable;
  private @Nullable JClass<?> receiverClass;

  @Override
  public JTypeResolver where(JTypeCapture<? extends @Nullable Object> typeParameter, JType type) {
    requireNonNull(typeParameter, "typeParameter");
    requireNonNull(type, "type");
    var classifier = typeParameter.type.classifier();
    if (classifier instanceof JTypeParameter) {
      // TODO: Merge nullability in a better way
      if (type.nullability() == Nullability.UNKNOWN && typeParameter.nullability() != Nullability.UNKNOWN) {
        type = type.withNullability(typeParameter.nullability());
      }
      return where((JTypeParameter) classifier, type);
    }
    throw new IllegalArgumentException("Expected a type parameter, not: " + typeParameter);
  }

  @Override
  public JTypeResolver where(JTypeParameter typeParameter, JType type) {
    requireNonNull(typeParameter, "typeParameter");
    requireNonNull(type, "type");
    var typeVariable = ((JTypeParameterImpl) typeParameter).typeVariable();
    var declaration = typeVariable.getGenericDeclaration();
    if (declaration instanceof Executable) {
      var executable = (Executable) declaration;
      if (this.executable != null && this.executable != executable) {
        throw new IllegalArgumentException("Cannot mix type parameters of different functions.");
      }
      checkReceiver(executable);
      this.executable = executable;
    }
    typeParameters.put(typeParameter, type);
    return this;
  }

  @Override
  public JTypeResolver where(JParameter parameter, JType type) {
    requireNonNull(parameter, "parameter");
    requireNonNull(type, "type");
    if (type.classifier() instanceof JTypeParameter) {
      // provides no useful info
      return this;
    }
    var executable = ((JParameterImpl) parameter).executable;
    if (this.executable != null && this.executable != executable) {
      throw new IllegalArgumentException("Cannot mix type parameters of different functions.");
    }
    checkReceiver(executable);
    this.executable = executable;
    var parameterType = parameter.type();
    var parameterClassifier = parameterType.classifier();
    if (parameterClassifier instanceof JTypeParameter) {
      typeParameters.put((JTypeParameter) parameterClassifier, type);
    } else {
      var supertype = type.asSupertype((JClass<?>) parameterClassifier);
      if (supertype == null) {
        throw new IllegalArgumentException("Provided type '" + type +
            "' is not a subtype of the parameter: " + parameter);
      }
      collectTypeParameters(parameterType, supertype);
    }
    return this;
  }

  @Override
  public JTypeResolver whereReturns(JFunction<? extends @Nullable Object> function, JType type) {
    requireNonNull(function, "function");
    requireNonNull(type, "type");
    if (type.classifier() instanceof JTypeParameter) {
      // provides no useful info
      return this;
    }
    var executable = ((JFunctionExecutableImpl<?>) function).executable;
    if (this.executable != null && this.executable != executable) {
      throw new IllegalArgumentException("Cannot mix type parameters of different functions.");
    }
    checkReceiver(executable);
    this.executable = executable;
    var parameterType = function.returnType();
    var parameterClassifier = parameterType.classifier();
    if (parameterClassifier instanceof JTypeParameter) {
      typeParameters.put((JTypeParameter) parameterClassifier, type);
    } else {
      var supertype = type.asSupertype((JClass<?>) parameterClassifier);
      if (supertype == null) {
        throw new IllegalArgumentException("Provided type '" + type +
            "' is not a subtype of the return type: " + function.returnType());
      }
      collectTypeParameters(parameterType, supertype);
    }
    return this;
  }

  private void checkReceiver(Executable executable) {
    if (receiverClass != null && !executable.getDeclaringClass().isAssignableFrom(receiverClass.javaClass())) {
      throw new IllegalArgumentException("Provided declaring class '" + executable.getDeclaringClass().getName() +
          "' is not a supertype of the declaring receiver: " + receiverClass.qualifiedName());
    }
  }

  @Override
  public JTypeResolver whereReceiver(JType type) {
    requireNonNull(type, "type");
    var classifier = type.classifier();
    if (classifier instanceof JTypeParameter) {
      // provides no useful info
      return this;
    }
    if (receiverClass != null) {
      throw new IllegalArgumentException("Receiver is already provided.");
    }
    if (executable != null) {
      var declaringClass = JClass.of(executable.getDeclaringClass());
      this.receiverClass = declaringClass;
      var supertype = type.asSupertype(declaringClass);
      if (supertype == null) {
        throw new IllegalArgumentException("Provided type '" + type +
            "' is not a subtype of the declaring receiver: " + executable.getDeclaringClass().getName());
      }
      collectTypeParameters(declaringClass.unresolvedType(), type);
    } else {
      // we don't know when to stop looking, so go through all the type parameters in the tree
      collectTypeParameters(type);
      for (var supertype : type.supertypes()) {
        collectTypeParameters(supertype);
      }
    }
    return this;
  }

  private void collectTypeParameters(JType resolved) {
    var classifier = resolved.classifier();
    if (classifier instanceof JClass<?>) {
      collectTypeParameters(classifier.unresolvedType(), resolved);
    }
  }

  private void collectTypeParameters(JType unresolved, JType resolved) {
    var unresolvedArguments = unresolved.arguments();
    var resolvedArguments = resolved.arguments();
    for (int i = 0; i < unresolvedArguments.size(); i++) {
      var unresolvedArgument = unresolvedArguments.get(i);
      var unresolvedType = unresolvedArgument.type();
      if (unresolvedType != null) {
        var resolvedArgument = resolvedArguments.get(i);
        var resolvedType = resolvedArgument.type();
        if (resolvedType != null) {
          if (unresolvedType.classifier() instanceof JTypeParameter) {
            typeParameters.put((JTypeParameter) unresolvedType.classifier(), resolvedType);
          } else {
            collectTypeParameters(unresolvedType, resolvedType);
          }
        }
      }
    }
  }

  @Override
  public JType resolve(JTypeParameter typeParameter) {
    requireNonNull(typeParameter, "parameter");
    var resolved = typeParameters.get(typeParameter);
    if (resolved != null) {
      return resolved;
    }
    return typeParameter.unresolvedType();
  }

  @Override
  public JType resolve(JType type) {
    requireNonNull(type, "type");
    return requireNonNullElse(resolveOrNull(type), type);
  }

  private @Nullable List<JType> resolveOrNull(List<JType> types) {
    List<JType> resolvedTypes = null;
    for (int i = 0; i < types.size(); i++) {
      var type = types.get(i);
      var resolvedType = resolveOrNull(type);
      if (resolvedType != null && resolvedType != type) {
        if (resolvedTypes == null) {
          resolvedTypes = new ArrayList<>(types.size());
          if (i > 0) {
            resolvedTypes.addAll(types.subList(0, i));
          }
        }
        resolvedTypes.add(resolvedType);
      } else if (resolvedTypes != null) {
        resolvedTypes.add(type);
      }
    }
    return resolvedTypes;
  }

  private @Nullable JType resolveOrNull(JType type) {
    var classifier = type.classifier();
    if (classifier instanceof JTypeParameter) {
      return typeParameters.get(classifier);
    }
    if (classifier instanceof JTypeIntersection) {
      var intersection = (JTypeIntersection) classifier;
      var resolved = resolveOrNull(intersection.types());
      return resolved == null ? null : JTypeImpl.intersectionOf(resolved, type.nullability());
    }
    var arguments = type.arguments();
    if (arguments.isEmpty()) {
      return null;
    }
    List<JTypeProjection> resolvedArguments = null;
    for (int i = 0; i < arguments.size(); i++) {
      var argument = arguments.get(i);
      var argumentType = argument.type();
      JType resolved = null;
      if (argumentType != null) {
        resolved = resolveOrNull(argumentType);
      }
      if (resolved != null && resolved != argumentType) {
        if (resolvedArguments == null) {
          resolvedArguments = new ArrayList<>(arguments.size());
          resolvedArguments.addAll(arguments.subList(0, i));
        }
        resolvedArguments.add(JTypeProjection.of(argument.variance(), resolved));
      } else if (resolvedArguments != null) {
        resolvedArguments.add(argument);
      }
    }
    if (resolvedArguments != null) {
      return classifier.createType(resolvedArguments, type.nullability(), type.annotations());
    }
    return null;
  }
}
