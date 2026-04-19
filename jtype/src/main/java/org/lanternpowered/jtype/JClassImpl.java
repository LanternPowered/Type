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
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class JClassImpl<T> implements JClass<T> {

  private static final WeakKeyConcurrentCache<Class<?>, JClassImpl<?>> cache =
      new WeakKeyConcurrentCache<>(JClassImpl::new);

  @SuppressWarnings("unchecked")
  static <T> JClassImpl<T> of(Class<T> type) {
    return (JClassImpl<T>) cache.get(type);
  }

  private final Class<T> clazz;
  private final String simpleName;
  private final Map<Executable, JFunctionExecutableImpl<?>> functionByExecutable = new ConcurrentHashMap<>();
  private @Nullable List<JTypeParameter> typeParameters;
  private @Nullable List<Annotation> annotations;
  private @Nullable List<JClass<?>> superclasses;
  private @Nullable List<JClass<?>> allSuperclasses;
  private @Nullable List<JType> supertypes;
  private @Nullable List<JType> allSupertypes;
  private @Nullable JType unresolvedType;
  private @Nullable JType starType;
  private @Nullable JClass<?> owner;

  private JClassImpl(Class<T> clazz) {
    this.clazz = clazz;
    this.simpleName = clazz.getSimpleName();
  }

  @SuppressWarnings("unchecked")
  <R> JFunctionExecutableImpl<R> function(Executable executable) {
    return (JFunctionExecutableImpl<R>) functionByExecutable.computeIfAbsent(executable, JFunctionExecutableImpl::new);
  }

  @Override
  public boolean isInstance(@Nullable Object value) {
    return clazz.isInstance(value);
  }

  @Override
  public Class<T> javaClass() {
    return clazz;
  }

  @Override
  public String name() {
    return simpleName;
  }

  @Override
  public String qualifiedName() {
    return clazz.getName();
  }

  @Override
  public List<JTypeParameter> typeParameters() {
    var typeParameters = this.typeParameters;
    if (typeParameters == null) {
      var originalTypeParameters = clazz.getTypeParameters();
      typeParameters = new ArrayList<>(originalTypeParameters.length);
      for (var originalTypeParameter : originalTypeParameters) {
        typeParameters.add(new JTypeParameterImpl(originalTypeParameter));
      }
      typeParameters = List.copyOf(typeParameters);
      this.typeParameters = typeParameters;
    }
    return typeParameters;
  }

  @Override
  public boolean isSuperclassOf(Class<?> derived) {
    return derived.isAssignableFrom(clazz);
  }

  @Override
  public boolean isSuperclassOf(JClass<?> derived) {
    return derived.javaClass().isAssignableFrom(clazz);
  }

  @Override
  public boolean isSubclassOf(Class<?> derived) {
    return clazz.isAssignableFrom(derived);
  }

  @Override
  public boolean isSubclassOf(JClass<?> base) {
    return clazz.isAssignableFrom(base.javaClass());
  }

  @Override
  public JType unresolvedType() {
    var unresolvedType = this.unresolvedType;
    if (unresolvedType == null) {
      var arguments = new ArrayList<JTypeProjection>();
      for (var typeParameter : typeParameters()) {
        arguments.add(JTypeProjection.invariant(typeParameter.unresolvedType()));
      }
      unresolvedType = new JTypeImpl(this, arguments, Nullability.NON_NULL, List.of());
      this.unresolvedType = unresolvedType;
    }
    return unresolvedType;
  }

  @Override
  public JType starType() {
    var starType = this.starType;
    if (starType == null) {
      var arguments = new ArrayList<JTypeProjection>();
      for (var typeParameter : typeParameters()) {
        arguments.add(JTypeProjection.invariant(typeParameter.starType()));
      }
      starType = new JTypeImpl(this, arguments, Nullability.NON_NULL, List.of());
      this.starType = starType;
    }
    return starType;
  }

  @Override
  public List<JClass<?>> superclasses() {
    var superclasses = this.superclasses;
    if (superclasses == null) {
      superclasses = collectSuperclasses(clazz, false);
      this.superclasses = superclasses;
    }
    return superclasses;
  }

  @Override
  public List<JClass<?>> allSuperclasses() {
    var allSuperclasses = this.allSuperclasses;
    if (allSuperclasses == null) {
      allSuperclasses = collectSuperclasses(clazz, true);
      this.allSuperclasses = allSuperclasses;
    }
    return allSuperclasses;
  }

  private static List<JClass<?>> collectSuperclasses(Class<?> type, boolean nested) {
    var superclasses = new ArrayList<JClass<?>>();
    collectSuperclasses(type, superclasses, nested);
    return List.copyOf(superclasses);
  }

  private static void collectSuperclasses(Class<?> type, List<JClass<?>> jClasses, boolean nested) {
    var superclass = type.getSuperclass();
    if (superclass != null) {
      var jClass = JClass.of(superclass);
      if (!jClasses.contains(jClass)) {
        jClasses.add(jClass);
        if (nested) {
          collectSuperclasses(superclass, jClasses, true);
        }
      }
    }
    for (var itf : type.getInterfaces()) {
      var jClass = JClass.of(itf);
      if (!jClasses.contains(jClass)) {
        jClasses.add(jClass);
        if (nested) {
          collectSuperclasses(itf, jClasses, true);
        }
      }
    }
  }

  @Override
  public List<JType> supertypes() {
    var supertypes = this.supertypes;
    if (supertypes == null) {
      supertypes = collectSupertypes(clazz, false);
      this.supertypes = supertypes;
    }
    return supertypes;
  }

  @Override
  public List<JType> allSupertypes() {
    var allSupertypes = this.allSupertypes;
    if (allSupertypes == null) {
      allSupertypes = collectSupertypes(clazz, true);
      this.allSupertypes = allSupertypes;
    }
    return allSupertypes;
  }

  private static List<JType> collectSupertypes(Class<?> type, boolean nested) {
    var supertypes = new ArrayList<JType>();
    collectSupertypes(type, supertypes, nested);
    return List.copyOf(supertypes);
  }

  private static Class<?> rawType(Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    }
    throw new IllegalArgumentException("Unexpected type: " + type);
  }

  private static void collectSupertypes(Class<?> type, List<JType> jTypes, boolean nested) {
    var superclass = type.getAnnotatedSuperclass();
    if (superclass != null) {
      var jType = JType.of(superclass);
      if (!jTypes.contains(jType)) {
        jTypes.add(jType);
        if (nested) {
          collectSupertypes(type.getSuperclass(), jTypes, true);
        }
      }
    }
    for (var itf : type.getAnnotatedInterfaces()) {
      var jType = JType.of(itf);
      if (!jTypes.contains(jType)) {
        jTypes.add(jType);
        if (nested) {
          collectSupertypes(rawType(itf.getType()), jTypes, true);
        }
      }
    }
  }

  @Override
  public boolean isFinal() {
    return Modifier.isFinal(clazz.getModifiers());
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(clazz.getModifiers());
  }

  @Override
  public @Nullable JClass<?> owner() {
    var owner = this.owner;
    if (owner == null) {
      var declaringClass = clazz.getDeclaringClass();
      if (declaringClass != null) {
        owner = of(declaringClass);
        this.owner = owner;
      }
    }
    return owner;
  }

  @Override
  public List<Annotation> annotations() {
    var annotations = this.annotations;
    if (annotations == null) {
      annotations = List.of(clazz.getAnnotations());
      this.annotations = annotations;
    }
    return annotations;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JClassImpl<?> && ((JClassImpl<?>) obj).clazz.equals(clazz);
  }

  @Override
  public int hashCode() {
    return clazz.hashCode();
  }

  @Override
  public String toString() {
    return clazz.getTypeName();
  }
}
