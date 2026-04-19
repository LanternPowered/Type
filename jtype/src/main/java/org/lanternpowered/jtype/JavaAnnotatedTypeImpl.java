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
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class JavaAnnotatedTypeImpl implements AnnotatedType {

  static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

  static JavaAnnotatedTypeImpl of(Type type, List<Annotation> annotations) {
    return of(type, annotations.toArray(Annotation[]::new));
  }

  static JavaAnnotatedTypeImpl of(Type type) {
    return of(type, EMPTY_ANNOTATIONS);
  }

  static @Nullable JavaAnnotatedTypeImpl ofNullable(@Nullable Type type) {
    return type == null ? null : of(type);
  }

  private static JavaAnnotatedTypeImpl of(Type type, Annotation[] annotations) {
    if (type instanceof Class<?>) {
      var rawType = (Class<?>) type;
      var ownerType = ofNullable(rawType.getEnclosingClass());
      return new JavaAnnotatedTypeImpl(type, annotations, ownerType, null);
    } else if (type instanceof ParameterizedType) {
      var parameterizedType = (ParameterizedType) type;
      var ownerType = ofNullable(parameterizedType.getOwnerType());
      var typeParameters = of(parameterizedType.getActualTypeArguments());
      return new JavaAnnotatedParameterizedTypeImpl(type, annotations, typeParameters, ownerType, null);
    } else if (type instanceof GenericArrayType) {
      var arrayType = (GenericArrayType) type;
      var componentType = of(arrayType.getGenericComponentType());
      return new JavaAnnotatedArrayTypeImpl(type, annotations, componentType, null);
    } else if (type instanceof TypeVariable) {
      var typeVariable = (TypeVariable<?>) type;
      var bounds = of(typeVariable.getBounds());
      return new JavaAnnotatedTypeVariableImpl(type, annotations, bounds, null);
    } else if (type instanceof WildcardType) {
      var wildcardType = (WildcardType) type;
      var lowerBounds = of(wildcardType.getLowerBounds());
      var upperBounds = of(wildcardType.getUpperBounds());
      return new JavaAnnotatedWildcardTypeImpl(type, annotations, lowerBounds, upperBounds, null);
    }
    throw new IllegalArgumentException("Unexpected type: " + type);
  }

  private static AnnotatedType[] of(Type[] typeArray) {
    var annotatedTypeArray = new AnnotatedType[typeArray.length];
    for (int i = 0; i < typeArray.length; i++) {
      annotatedTypeArray[i] = of(typeArray[i]);
    }
    return annotatedTypeArray;
  }

  protected final @Nullable AnnotatedType ownerType;
  protected final Type type;
  protected final Annotation[] annotations;
  protected final @Nullable JType jType;

  JavaAnnotatedTypeImpl(Type type, Annotation[] annotations, @Nullable AnnotatedType ownerType, @Nullable JType jType) {
    this.ownerType = ownerType;
    this.type = type;
    this.annotations = annotations;
    this.jType = jType;
  }

  @Override
  public @Nullable AnnotatedType getAnnotatedOwnerType() {
    return ownerType;
  }

  @Override
  public Type getType() {
    return type;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Annotation> @Nullable T getAnnotation(Class<T> annotationClass) {
    return (T) Arrays.stream(annotations).filter(annotationClass::isInstance).findFirst().orElse(null);
  }

  @Override
  public Annotation[] getAnnotations() {
    return Arrays.copyOf(annotations, annotations.length);
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }

  private static Annotation[] annotations(AnnotatedType type) {
    return type instanceof JavaAnnotatedTypeImpl ? ((JavaAnnotatedTypeImpl) type).annotations : type.getAnnotations();
  }

  protected boolean typeAndAnnotationEquals(AnnotatedType other) {
    return type.equals(other.getType()) &&
        Arrays.equals(annotations, annotations(other)) &&
        Objects.equals(ownerType, other.getAnnotatedOwnerType());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof AnnotatedType &&
        !(obj instanceof AnnotatedWildcardType) &&
        !(obj instanceof AnnotatedArrayType) &&
        !(obj instanceof AnnotatedTypeVariable) &&
        !(obj instanceof AnnotatedParameterizedType)) {
      return typeAndAnnotationEquals((AnnotatedType) obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, Arrays.hashCode(annotations), ownerType);
  }

  protected String rawTypeName() {
    if (type instanceof Class<?>) {
      return type.getTypeName();
    } else if (type instanceof ParameterizedType) {
      return ((ParameterizedType) type).getRawType().getTypeName();
    }
    return type.toString();
  }

  @Override
  public String toString() {
    var type = rawTypeName();
    if (annotations.length == 0) {
      return type;
    }
    var builder = new StringBuilder();
    for (var annotation : annotations) {
      builder.append(annotation);
      builder.append(' ');
    }
    return builder.append(type).toString();
  }
}
