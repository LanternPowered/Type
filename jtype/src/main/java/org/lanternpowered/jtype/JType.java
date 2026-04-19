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
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface JType extends JAnnotatedElement {

  /**
   * Returns a {@link JType} representation of the given {@link Type}.
   */
  static JType of(Type type) {
    return JTypeImpl.of(requireNonNull(type, "type"));
  }

  /**
   * Returns a {@link JType} representation of the given {@link Type} and annotations.
   */
  static JType of(Type type, List<Annotation> annotations) {
    return JTypeImpl.of(requireNonNull(type, "type"), List.copyOf(annotations));
  }

  /**
   * Returns a {@link JType} representation of the given {@link AnnotatedType}.
   */
  static JType of(AnnotatedType type) {
    return JTypeImpl.of(requireNonNull(type, "type"));
  }

  /**
   * Returns an intersection of the given {@link JType}s. At least one type must be provided.
   */
  static JType intersectionOf(JType... types) {
    return intersectionOf(List.of(types));
  }

  /**
   * Returns an intersection of the given {@link JType}s. At least one type must be provided.
   */
  static JType intersectionOf(List<JType> types) {
    return JTypeImpl.intersectionOf(types, null);
  }

  /**
   * Returns the java {@link AnnotatedType} representation of this type.
   */
  AnnotatedType javaType();

  /**
   * Returns the classifier. Can be a {@link JClass}, {@link JTypeIntersection} or {@link JTypeParameter}.
   */
  JClassifier classifier();

  /**
   * Type arguments passed for the parameters of the classifier in this type.
   * For example, in the type {@code List<? extends Number>} the only type argument is {@code ? extends Number}.
   * <p>
   * In case this type is based on an inner class, the returned list contains the type arguments provided
   * for the innermost class first, then its outer class, and so on.
   * For example, in the type {@code Outer<A, B>.Inner<C, D>} the returned list is {@code [C, D, A, B]}.
   */
  List<JTypeProjection> arguments();

  /**
   * Returns the nullability of this type. This gives no guarantees of the actual nullability of a value, merely
   * an indicator depending on the applied annotations.
   */
  Nullability nullability();

  /**
   * Returns a {@link JType} with the given nullability.
   */
  JType withNullability(Nullability nullability);

  /**
   * Returns if this {@link JType} is a supertype of the given type.
   */
  boolean isSupertypeOf(JType derived);

  /**
   * Returns if this {@link JType} is a subtype of the given type.
   */
  boolean isSubtypeOf(JType base);

  /**
   * Returns a list with all the direct superclasses.
   */
  List<JClass<?>> superclasses();

  /**
   * Returns a list with all the superclasses.
   */
  List<JClass<?>> allSuperclasses();

  /**
   * Returns a list with all the direct supertypes.
   */
  List<JType> supertypes();

  /**
   * Returns a list with all the supertypes.
   */
  List<JType> allSupertypes();

  /**
   * Returns a supertype that for the given base. Returns {@code null} if this
   * is type is not a subtype of the {@code base}.
   * <p>
   * For example if this type is {@code ArrayList<Integer>}, calling this with
   * {@code asSupertype(List.class)} will result in {@code List<Integer>}.
   */
  default @Nullable JType asSupertype(Class<?> base) {
    return asSupertype(JClass.of(base));
  }

  /**
   * Returns a supertype that for the given base. Returns {@code null} if this
   * is type is not a subtype of the {@code base}.
   * <p>
   * For example if this type is {@code ArrayList<Integer>}, calling this with
   * {@code asSupertype(List.class)} will result in {@code List<Integer>}.
   */
  @Nullable JType asSupertype(JClass<?> base);

  /**
   * Enriches this type with the given reified type information.
   * <p>
   * For example, when reifying an {@code ArrayList} instance which is stored in a parameter with the type
   * {@code List<Integer>}. The returned reified type will be {@code ArrayList<Integer>}.
   */
  JType reify(JType reifiedType);

  /**
   * Resolves this type by replacing unresolved type parameters by their bounds.
   */
  JType resolve();
}
