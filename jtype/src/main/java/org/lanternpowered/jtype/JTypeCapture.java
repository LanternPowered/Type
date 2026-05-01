/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("unused")
public abstract class JTypeCapture<T extends @Nullable Object> implements JType, GenericType<T> {

  final JType type;

  public JTypeCapture() {
    type = JTypeCaptureResolver.resolve(this);
  }

  @Override
  public final AnnotatedType javaType() {
    return this.type.javaType();
  }

  @SuppressWarnings("unchecked")
  @Override
  public final JClass<? super @NonNull T> rawType() {
    return (JClass<? super @NonNull T>) this.type.rawType();
  }

  @Override
  public final List<JClass<?>> superclasses() {
    return this.type.superclasses();
  }

  @Override
  public final List<JClass<?>> allSuperclasses() {
    return this.type.allSuperclasses();
  }

  @Override
  public final List<JType> supertypes() {
    return this.type.supertypes();
  }

  @Override
  public final List<JType> allSupertypes() {
    return this.type.allSupertypes();
  }

  @Override
  public final boolean isSubtypeOf(Type base) {
    return this.type.isSubtypeOf(base);
  }

  @Override
  public final boolean isSubtypeOf(JType base) {
    return this.type.isSubtypeOf(base);
  }

  @Override
  public final boolean isSupertypeOf(Type derived) {
    return this.type.isSupertypeOf(derived);
  }

  @Override
  public final boolean isSupertypeOf(JType derived) {
    return this.type.isSupertypeOf(derived);
  }

  @Override
  public final @Nullable JType asSupertype(Class<?> base) {
    return this.type.asSupertype(base);
  }

  @Override
  public final @Nullable JType asSupertype(JClass<?> base) {
    return this.type.asSupertype(base);
  }

  @Override
  public final JType reify(JType reifiedType) {
    return this.type.reify(reifiedType);
  }

  @Override
  public final JType resolve() {
    return this.type.resolve();
  }

  @Override
  public final JType boxed() {
    return this.type.boxed();
  }

  @Override
  public final JClassifier classifier() {
    return this.type.classifier();
  }

  @Override
  public final List<JTypeProjection> arguments() {
    return this.type.arguments();
  }

  @Override
  public final Nullability nullability() {
    return this.type.nullability();
  }

  @SuppressWarnings("unchecked")
  @NullUnmarked
  @Override
  public final @NonNull GenericType<T> withNullability(@NonNull Nullability nullability)  {
    return (GenericType<T>) this.type.withNullability(nullability);
  }

  @Override
  public final List<Annotation> annotations() {
    return this.type.annotations();
  }

  @Override
  public final <A extends Annotation> @Nullable A findAnnotation(Class<A> type) {
    return this.type.findAnnotation(type);
  }

  @Override
  public final <A extends Annotation> @Nullable A findAnnotation(JClass<A> type) {
    return this.type.findAnnotation(type);
  }

  @Override
  public final <A extends Annotation> List<A> findAnnotations(Class<A> type) {
    return this.type.findAnnotations(type);
  }

  @Override
  public final <A extends Annotation> List<A> findAnnotations(JClass<A> type) {
    return this.type.findAnnotations(type);
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public final boolean equals(@Nullable Object obj) {
    return this.type.equals(obj);
  }

  @Override
  public final int hashCode() {
    return this.type.hashCode();
  }

  @Override
  public final String toString() {
    return this.type.toString();
  }

  @Override
  protected final Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
