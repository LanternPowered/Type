/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.lanternpowered.jtype.JavaAnnotatedTypeImpl.EMPTY_ANNOTATIONS;

final class JTypeImpl implements JType {

  private static final Set<String> NULLABLE_ANNOTATIONS = Set.of(
      "org.checkerframework.checker.nullness.qual.Nullable",
      "javax.annotation.Nullable",
      "jakarta.annotation.Nullable",
      "org.jetbrains.annotations.Nullable",
      "org.jspecify.annotations.Nullable"
  );

  private static final Set<String> NON_NULL_ANNOTATIONS = Set.of(
      "org.checkerframework.checker.nullness.qual.NonNull",
      "javax.annotation.Nonnull",
      "jakarta.annotation.Nonnull",
      "org.jetbrains.annotations.NotNull",
      "org.jspecify.annotations.NonNull"
  );

  static JType intersectionOf(List<JType> types, @Nullable Nullability nullability) {
    if (types.isEmpty()) {
      throw new IllegalArgumentException("At least one type expected.");
    }
    if (types.size() == 1) {
      var type = types.get(0);
      if (nullability != null) {
        type = type.withNullability(nullability);
      }
      return type;
    }
    var intersection = new JTypeIntersectionImpl(List.copyOf(types));
    if (nullability == null || nullability == Nullability.UNKNOWN) {
      nullability = nullabilityOfTypes(types);
    }
    return new JTypeImpl(intersection, List.of(), nullability, List.of());
  }

  static JTypeImpl of(Type type) {
    return of(type, List.of());
  }

  static JTypeImpl of(Type type, List<Annotation> annotations) {
    if (type instanceof Class<?>) {
      return of((Class<?>) type, annotations);
    } else if (type instanceof ParameterizedType) {
      return of((ParameterizedType) type, annotations);
    } else if (type instanceof GenericArrayType) {
      return of((GenericArrayType) type, annotations);
    } else if (type instanceof WildcardType) {
      return of((WildcardType) type, annotations);
    } else if (type instanceof TypeVariable) {
      return of((TypeVariable<?>) type, annotations);
    }
    throw new IllegalArgumentException("Unexpected type: " + type);
  }

  private static JTypeImpl of(Class<?> type, List<Annotation> annotations) {
    var classifier = JClass.of((Class<?>) type);
    var arguments = new ArrayList<JTypeProjection>();
    collectTypeProjections(type, arguments);
    return new JTypeImpl(classifier, List.copyOf(arguments), type, annotations);
  }

  private static JTypeImpl of(ParameterizedType type, List<Annotation> annotations) {
    var classifier = JClass.of((Class<?>) type.getRawType());
    var arguments = new ArrayList<JTypeProjection>();
    collectTypeProjections(type, arguments);
    return new JTypeImpl(classifier, List.copyOf(arguments), type, annotations);
  }

  private static JTypeImpl of(GenericArrayType type, List<Annotation> annotations) {
    var classifier = JClass.of(Object[].class);
    var arguments = List.of(JTypeProjection.invariant(of(type.getGenericComponentType())));
    return new JTypeImpl(classifier, arguments, type, annotations);
  }

  private static JTypeImpl of(WildcardType type, List<Annotation> annotations) {
    var boundType = boundsToType(type.getUpperBounds());
    return boundType != null ? boundType : of(Object.class, annotations);
  }

  static final ThreadLocal<@Nullable JFunctionExecutableImpl<?>> currentFunction = new ThreadLocal<>();

  private static JTypeImpl of(TypeVariable<?> type, List<Annotation> annotations) {
    var declaration = type.getGenericDeclaration();
    if (declaration instanceof Executable) {
      var executable = (Executable) declaration;
      var jClass = JClassImpl.of(executable.getDeclaringClass());
      var jFunction = currentFunction.get();
      if (jFunction == null) {
        jFunction = jClass.function(executable);
      }
      var finalJFunction = jFunction;
      var typeParameter = jFunction.typeParameters().stream()
          .filter(parameter -> parameter.name().equals(type.getName()))
          .findFirst().orElseThrow(() -> new IllegalArgumentException(
              "Failed to find TypeParameter with name " + type.getName() + " in function " + finalJFunction));
      return new JTypeImpl(typeParameter, List.of(), type, annotations);
    } else if (declaration instanceof Class<?>) {
      var rawType = (Class<?>) declaration;
      var jClass = JClassImpl.of(rawType);
      var typeParameter = jClass.typeParameters().stream()
          .filter(parameter -> parameter.name().equals(type.getName()))
          .findFirst().orElseThrow(() -> new IllegalArgumentException(
              "Failed to find TypeParameter with name " + type.getName() + " in class " + jClass));
      return new JTypeImpl(typeParameter, List.of(), type, annotations);
    }
    throw new IllegalArgumentException("Unexpected declaration: " + declaration);
  }

  static JTypeImpl of(AnnotatedType type) {
    if (type instanceof JavaAnnotatedTypeImpl) {
      var jType = ((JavaAnnotatedTypeImpl) type).jType;
      if (jType != null) {
        return (JTypeImpl) jType;
      }
    }
    if (type instanceof AnnotatedParameterizedType) {
      return of((AnnotatedParameterizedType) type);
    } else if (type instanceof AnnotatedArrayType) {
      return of((AnnotatedArrayType) type);
    } else if (type instanceof AnnotatedWildcardType) {
      return of((AnnotatedWildcardType) type);
    } else {
      return of(type.getType(), List.of(type.getAnnotations()));
    }
  }

  private static JTypeImpl of(AnnotatedParameterizedType type) {
    var classifier = JClass.of((Class<?>) ((ParameterizedType) type.getType()).getRawType());
    var arguments = new ArrayList<JTypeProjection>();
    collectTypeProjections(type, arguments);
    return new JTypeImpl(classifier, arguments, type, List.of(type.getAnnotations()));
  }

  private static JTypeImpl of(AnnotatedArrayType type) {
    var classifier = JClass.of(Object[].class);
    var arguments = List.of(JTypeProjection.invariant(of(type.getAnnotatedGenericComponentType())));
    return new JTypeImpl(classifier, arguments, type, List.of(type.getAnnotations()));
  }

  private static JTypeImpl of(AnnotatedWildcardType type) {
    var boundType = boundsToType(type.getAnnotatedUpperBounds());
    // TODO: Add annotations?
    return boundType != null ? boundType : of(Object.class);
  }

  static Nullability nullabilityOfAnnotations(List<Annotation> annotations) {
    var anyNonNull = false;
    for (var annotation : annotations) {
      var name = annotation.annotationType().getName();
      if (NULLABLE_ANNOTATIONS.contains(name)) {
        return Nullability.NULLABLE;
      } else if (NON_NULL_ANNOTATIONS.contains(name)) {
        anyNonNull = true;
      }
    }
    return anyNonNull ? Nullability.NON_NULL : Nullability.UNKNOWN;
  }

  static Nullability nullabilityOfTypes(List<JType> types) {
    var anyUnknown = false;
    for (var type : types) {
      var nullability = type.nullability();
      if (nullability == Nullability.NULLABLE) {
        return Nullability.NULLABLE;
      } else if (nullability == Nullability.UNKNOWN) {
        anyUnknown = true;
      }
    }
    return anyUnknown ? Nullability.UNKNOWN : Nullability.NON_NULL;
  }

  private static void collectTypeProjections(Type type, List<JTypeProjection> projections) {
    if (type instanceof ParameterizedType) {
      var parameterizedType = (ParameterizedType) type;
      for (var typeArgument : parameterizedType.getActualTypeArguments()) {
        projections.add(toTypeProjection(typeArgument));
      }
      var ownerType = parameterizedType.getOwnerType();
      if (ownerType != null) {
        collectTypeProjections(ownerType, projections);
      }
    } else if (type instanceof Class<?>) {
      var rawType = (Class<?>) type;
      for (int i = 0; i < rawType.getTypeParameters().length; i++) {
        projections.add(JTypeProjection.star());
      }
      var enclosingClass = rawType.getEnclosingClass();
      if (enclosingClass != null) {
        collectTypeProjections(enclosingClass, projections);
      }
    }
  }

  private static void collectTypeProjections(AnnotatedType type, List<JTypeProjection> projections) {
    if (type instanceof AnnotatedParameterizedType) {
      var parameterizedType = (AnnotatedParameterizedType) type;
      for (var typeArgument : parameterizedType.getAnnotatedActualTypeArguments()) {
        projections.add(toTypeProjection(typeArgument));
      }
    }
    var ownerType = type.getAnnotatedOwnerType();
    if (ownerType != null) {
      collectTypeProjections(ownerType, projections);
    }
  }

  private static JTypeProjection toTypeProjection(Type type) {
    if (type instanceof WildcardType) {
      return toTypeProjection((WildcardType) type);
    }
    return JTypeProjection.invariant(of(type));
  }

  static @Nullable JTypeImpl boundsToType(Type[] bounds) {
    List<JType> jTypes = null;
    JTypeImpl jType = null;
    for (var bound : bounds) {
      if (bound == Object.class) {
        continue;
      }
      if (jType != null) {
        if (jTypes == null) {
          jTypes = new ArrayList<>();
          jTypes.add(jType);
        }
        jTypes.add(of(bound));
      } else {
        jType = of(bound);
      }
    }
    if (jTypes != null) {
      var intersection = new JTypeIntersectionImpl(List.copyOf(jTypes));
      var nullability = nullabilityOfTypes(jTypes);
      jType = new JTypeImpl(intersection, List.of(), nullability, List.of());
    }
    return jType;
  }

  private static JTypeProjection toTypeProjection(WildcardType type) {
    var boundsType = boundsToType(type.getLowerBounds());
    if (boundsType != null) {
      return JTypeProjection.contravariant(boundsType);
    }
    boundsType = boundsToType(type.getUpperBounds());
    if (boundsType != null) {
      return JTypeProjection.covariant(boundsType);
    }
    return JTypeProjection.star();
  }

  private static JTypeProjection toTypeProjection(AnnotatedType type) {
    if (type instanceof AnnotatedWildcardType) {
      return toTypeProjection((AnnotatedWildcardType) type);
    }
    return JTypeProjection.invariant(of(type));
  }

  static @Nullable JTypeImpl boundsToType(AnnotatedType[] bounds) {
    List<JType> jTypes = null;
    JTypeImpl jType = null;
    for (var bound : bounds) {
      if (bound.getType() == Object.class && bound.getAnnotations().length == 0) {
        continue;
      }
      if (jType != null) {
        if (jTypes == null) {
          jTypes = new ArrayList<>();
          jTypes.add(jType);
        }
        jTypes.add(of(bound));
      } else {
        jType = of(bound);
      }
    }
    if (jTypes != null) {
      var intersection = new JTypeIntersectionImpl(List.copyOf(jTypes));
      var nullability = nullabilityOfTypes(jTypes);
      jType = new JTypeImpl(intersection, List.of(), nullability, List.of());
    }
    return jType;
  }

  private static JTypeProjection toTypeProjection(AnnotatedWildcardType type) {
    var boundsType = boundsToType(type.getAnnotatedLowerBounds());
    if (boundsType != null) {
      return JTypeProjection.contravariant(boundsType);
    }
    boundsType = boundsToType(type.getAnnotatedUpperBounds());
    if (boundsType != null) {
      return JTypeProjection.covariant(boundsType);
    }
    return JTypeProjection.star();
  }

  private static Stream<JType> resolveAllSupertypes(JType type) {
    return resolveSupertypes(type).stream()
        .flatMap(supertype -> Stream.concat(Stream.of(supertype), resolveAllSupertypes(supertype)))
        .distinct();
  }

  private static List<JType> resolveSupertypes(JType type) {
    var classifier = type.classifier();
    if (classifier instanceof JClass<?>) {
      var jClass = (JClass<?>) classifier;
      return List.copyOf(jClass.supertypes().stream()
          .map(supertype -> resolveSupertype(supertype, type))
          .collect(Collectors.toList()));
    } else if (classifier instanceof JTypeParameter) {
      return List.of();
    }
    throw new IllegalArgumentException("Unexpected classifier: " + classifier);
  }

  private static JType resolveSupertype(JType type, JType subType) {
    var subArgumentsByTypeParameter = new HashMap<JTypeParameter, JTypeProjection>();
    var subArguments = subType.arguments();
    var subTypeParameters = ((JClass<?>) subType.classifier()).typeParameters();
    for (int i = 0; i < subTypeParameters.size(); i++) {
      var subTypeParameter = subTypeParameters.get(i);
      var subArgument = subArguments.get(i);
      subArgumentsByTypeParameter.put(subTypeParameter, subArgument);
    }
    return resolveTypeParameters(type, subArgumentsByTypeParameter);
  }

  private static JType resolveTypeParameters(JType type, Map<JTypeParameter, JTypeProjection> typeProjections) {
    var classifier = type.classifier();
    var arguments = type.arguments();
    if (arguments.isEmpty()) {
      return type;
    }
    var resolvedArguments = new ArrayList<JTypeProjection>(arguments.size());
    var modified = false;
    for (var argument : arguments) {
      var argumentType = argument.type();
      var argumentClassifier = argumentType == null ? null : argumentType.classifier();
      JTypeProjection resolved = null;
      if (argumentClassifier instanceof JTypeParameter) {
        resolved = typeProjections.get(argumentClassifier);
      } else if (argumentType != null) {
        var resolvedType = resolveTypeParameters(argumentType, typeProjections);
        if (resolvedType != argumentType) {
          resolved = JTypeProjection.of(argument.variance(), resolvedType);
        }
      }
      if (resolved != null) {
        modified = true;
        resolvedArguments.add(resolved);
      } else {
        resolvedArguments.add(argument);
      }
    }
    return modified ? classifier.createType(resolvedArguments, type.nullability(), type.annotations()) : type;
  }

  private final JClassifier classifier;
  private final List<JTypeProjection> arguments;
  private final Nullability nullability;
  private final List<Annotation> annotations;
  private int hash;
  private @Nullable Type javaType;
  private @Nullable AnnotatedType annotatedJavaType;
  private @Nullable List<JType> supertypes;
  private @Nullable List<JType> allSupertypes;

  JTypeImpl(JClassifier classifier, List<JTypeProjection> arguments, Type javaType, List<Annotation> annotations) {
    this.classifier = classifier;
    this.arguments = arguments;
    this.annotations = annotations;
    this.nullability = nullabilityOfAnnotations(annotations);
    this.javaType = javaType;
  }

  JTypeImpl(JClassifier classifier, List<JTypeProjection> arguments, AnnotatedType javaType, List<Annotation> annotations) {
    this.classifier = classifier;
    this.arguments = arguments;
    this.annotations = List.of(javaType.getAnnotations());
    this.nullability = nullabilityOfAnnotations(annotations);
    this.annotatedJavaType = javaType;
  }

  JTypeImpl(JClassifier classifier, List<JTypeProjection> arguments, Nullability nullability, List<Annotation> annotations) {
    this.classifier = classifier;
    this.arguments = arguments;
    this.nullability = nullability;
    this.annotations = annotations;
  }

  @Override
  public AnnotatedType javaType() {
    var annotatedJavaType = this.annotatedJavaType;
    if (annotatedJavaType == null) {
      var javaType = this.javaType;
      if (javaType == null) {
        annotatedJavaType = toJavaType();
      } else {
        annotatedJavaType = JavaAnnotatedTypeImpl.of(javaType);
      }
      this.annotatedJavaType = annotatedJavaType;
    }
    return annotatedJavaType;
  }

  private AnnotatedType toJavaType() {
    var annotations = this.annotations.toArray(Annotation[]::new);
    if (classifier instanceof JTypeParameter) {
      var impl = (JTypeParameterImpl) classifier;
      var typeVariable = impl.typeVariable();
      var bounds = impl.upperBound();
      AnnotatedType[] annotatedBounds;
      if (bounds.classifier() instanceof JTypeIntersection) {
        annotatedBounds = ((JTypeIntersection) bounds.classifier()).types().stream()
            .map(JType::javaType).toArray(AnnotatedType[]::new);
      } else {
        annotatedBounds = new AnnotatedType[] { bounds.javaType() };
      }
      return new JavaAnnotatedTypeVariableImpl(typeVariable, annotations, annotatedBounds, this);
    } else if (classifier instanceof JTypeIntersection) {
      var intersection = (JTypeIntersection) classifier;
      var types = intersection.types();
      var annotatedBounds = types.stream().map(JType::javaType).toArray(AnnotatedType[]::new);
      var bounds = Arrays.stream(annotatedBounds).map(AnnotatedType::getType).toArray(Type[]::new);
      var javaType = new JavaWildcardTypeImpl(bounds, EMPTY_TYPES);
      return new JavaAnnotatedWildcardTypeImpl(javaType, annotations, annotatedBounds, EMPTY_ANNOTATED_TYPES, this);
    } else {
      // TODO: Owner
      var jClass = (JClass<?>) classifier;
      var javaClass = jClass.javaClass();
      if (javaClass.isArray()) {
        if (javaClass.componentType().isPrimitive()) {
          return new JavaAnnotatedTypeImpl(javaClass, annotations, null, this);
        }
        if (!arguments.isEmpty()) {
          var argument = arguments.get(0);
          var variance = argument.variance();
          if (variance == null || variance == JVariance.IN) {
            return new JavaAnnotatedTypeImpl(javaClass, annotations, null, this);
          }
          var componentType = requireNonNull(argument.type()).javaType();
          var javaType = new JavaArrayTypeImpl(componentType.getType());
          return new JavaAnnotatedArrayTypeImpl(javaType, annotations, componentType, this);
        } else {
          throw new UnsupportedOperationException();
        }
      } else if (!arguments.isEmpty()) {
        var annotatedParameters = new AnnotatedType[arguments.size()];
        var parameters = new Type[annotatedParameters.length];
        for (int i = 0; i < arguments.size(); i++) {
          annotatedParameters[i] = toJavaType(arguments.get(i));
          parameters[i] = annotatedParameters[i].getType();
        }
        var javaType = new JavaParameterizedTypeImpl(javaClass, parameters, null);
        return new JavaAnnotatedParameterizedTypeImpl(
            javaType, annotations, annotatedParameters, null, this);
      } else {
        return new JavaAnnotatedTypeImpl(javaClass, annotations, null, this);
      }
    }
  }

  static final AnnotatedType ANNOTATED_OBJECT_TYPE =
      new JavaAnnotatedTypeImpl(Object.class, EMPTY_ANNOTATIONS, null, null);
  private static final AnnotatedType[] EMPTY_ANNOTATED_TYPES = new AnnotatedType[0];
  private static final AnnotatedWildcardType STAR;
  private static final Type[] EMPTY_TYPES = new Type[0];

  static final JTypeImpl OBJECT = new JTypeImpl(JClass.of(Object.class), List.of(), Nullability.UNKNOWN, List.of());

  static {
    STAR = new JavaAnnotatedWildcardTypeImpl(
        new JavaWildcardTypeImpl(new Type[] { Object.class }, EMPTY_TYPES),
        EMPTY_ANNOTATIONS,
        new AnnotatedType[] { ANNOTATED_OBJECT_TYPE },
        EMPTY_ANNOTATED_TYPES, null);
  }

  private static AnnotatedType toJavaType(JTypeProjection typeProjection) {
    var type = (JTypeImpl) typeProjection.type();
    var variance = typeProjection.variance();
    if (type == null && variance == null) {
      return STAR;
    } else {
      var bound = requireNonNull(type, "type").javaType();
      if (variance == JVariance.INVARIANT) {
        return bound;
      }
      var bounds = new AnnotatedType[] { bound };
      var out = variance == JVariance.OUT;
      return new JavaAnnotatedWildcardTypeImpl(
          bound.getType(),
          EMPTY_ANNOTATIONS,
          out ? EMPTY_ANNOTATED_TYPES : bounds,
          out ? bounds : EMPTY_ANNOTATED_TYPES,
          null);
    }
  }

  @Override
  public JClassifier classifier() {
    return classifier;
  }

  @Override
  public List<JTypeProjection> arguments() {
    return arguments;
  }

  @Override
  public Nullability nullability() {
    return nullability;
  }

  private static final class AnnotationImpls {
    public static final @Nullable Object NULLABLE_FIELD = null;
    @SuppressWarnings("NullableProblems")
    public static final @NonNull Object NON_NULL_FIELD = new Object();
    public static final Nullable NULLABLE;
    public static final NonNull NON_NULL;
    static {
      try {
        NULLABLE = (Nullable) AnnotationImpls.class.getField("NULLABLE_FIELD").getAnnotations()[0];
        NON_NULL = (NonNull) AnnotationImpls.class.getField("NON_NULL_FIELD").getAnnotations()[0];
      } catch (NoSuchFieldException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

  @Override
  public JType withNullability(Nullability nullability) {
    if (nullability == this.nullability) {
      return this;
    }
    var annotations = new ArrayList<>(this.annotations);
    if (this.nullability == Nullability.NULLABLE) {
      annotations.removeIf(annotation -> NULLABLE_ANNOTATIONS.contains(annotation.annotationType().getName()));
    } else if (this.nullability == Nullability.NON_NULL) {
      annotations.removeIf(annotation -> NON_NULL_ANNOTATIONS.contains(annotation.annotationType().getName()));
    }
    if (nullability == Nullability.NULLABLE) {
      annotations.add(AnnotationImpls.NULLABLE);
    } else if (nullability == Nullability.NON_NULL) {
      annotations.add(AnnotationImpls.NON_NULL);
    }
    return new JTypeImpl(classifier, arguments, nullability, List.copyOf(annotations));
  }

  @Override
  public boolean isSupertypeOf(JType derived) {
    return derived.isSubtypeOf(this);
  }

  @Override
  public boolean isSubtypeOf(JType supertype) {
    if (nullability == Nullability.NON_NULL && supertype.nullability() == Nullability.NULLABLE) {
      return false;
    }
    var classifier = supertype.classifier();
    if (this.classifier instanceof JClass<?> && classifier instanceof JClass<?> &&
        !((JClass<?>) this.classifier).isSubclassOf((JClass<?>) classifier)) {
      var superclass = (JClass<?>) classifier;
      var subclass = (JClass<?>) this.classifier;
      if (!subclass.isSubclassOf(superclass)) {
        return false;
      }
      if (superclass.typeParameters().isEmpty() && subclass.typeParameters().isEmpty()) {
        return true;
      }
    }
    var subtype = findSupertype(this, classifier);
    if (subtype == null || (subtype.nullability() == Nullability.NON_NULL &&
        supertype.nullability() == Nullability.NULLABLE)) {
      return false;
    }
    return isSubtypeOf(subtype, supertype);
  }

  private static boolean isSubtypeOf(JType subtype, JType supertype) {
    if (supertype.nullability() == Nullability.NON_NULL && subtype.nullability() == Nullability.NULLABLE) {
      return false;
    }
    var superClassifier = supertype.classifier();
    var subClassifier = subtype.classifier();
    if (subClassifier instanceof JClass<?> && superClassifier instanceof JClass<?> &&
        !((JClass<?>) subClassifier).isSubclassOf((JClass<?>) superClassifier)) {
      var superclass = (JClass<?>) superClassifier;
      var subclass = (JClass<?>) subClassifier;
      if (!subclass.isSubclassOf(superclass)) {
        return false;
      }
      if (superclass.typeParameters().isEmpty() && subclass.typeParameters().isEmpty()) {
        return true;
      }
    }
    subtype = findSupertype(subtype, superClassifier);
    requireNonNull(subtype, "subtype");
    if (supertype.nullability() == Nullability.NON_NULL && subtype.nullability() == Nullability.NULLABLE) {
      return false;
    }
    var superArgs = supertype.arguments();
    var subArgs = subtype.arguments();
    if (superArgs.size() != subArgs.size()) {
      return false; // TODO: One of the types is incorrect?
    }
    for (int i = 0; i < superArgs.size(); i++) {
      var superArg = superArgs.get(i);
      var subArg = subArgs.get(i);
      if (superArg.variance() == null) {
        continue; // star
      }
      if (superArg.variance() == JVariance.INVARIANT && subArg.variance() == JVariance.INVARIANT) {
        if (!isSubtypeOf(superArg.type(), subArg.type())) {
          return false;
        }
        continue;
      }
      // TODO
    }
    return true;
  }

  private static boolean isSameType(JType a, JType b) {
    return a.nullability() == b.nullability() &&
        a.classifier().equals(b.classifier());
  }

  private static @Nullable JType findSupertype(JType type, JClassifier classifier) {
    if (classifier instanceof JClass<?>) {
      return findSupertype(type, (JClass<?>) classifier);
    }
    return null;
  }

  private static @Nullable JType findSupertype(JType type, JClass<?> jClass) {
    var supertypes = type.supertypes();
    for (var supertype : supertypes) {
      var superclass = (JClass<?>) supertype.classifier();
      if (superclass == jClass) {
        return supertype;
      } else if (jClass.isSuperclassOf(superclass)) {
        var nested = findSupertype(supertype, jClass);
        if (nested != null) {
          return nested;
        }
      }
    }
    return null;
  }

  @Override
  public List<Annotation> annotations() {
    return annotations;
  }

  @Override
  public List<JClass<?>> superclasses() {
    if (classifier instanceof JClass<?>) {
      return ((JClass<?>) classifier).superclasses();
    }
    return List.of();
  }

  @Override
  public List<JClass<?>> allSuperclasses() {
    if (classifier instanceof JClass<?>) {
      return ((JClass<?>) classifier).allSuperclasses();
    }
    return List.of();
  }

  @Override
  public List<JType> supertypes() {
    var supertypes = this.supertypes;
    if (supertypes == null) {
      supertypes = resolveSupertypes(this);
      this.supertypes = supertypes;
    }
    return supertypes;
  }

  @Override
  public List<JType> allSupertypes() {
    var allSupertypes = this.allSupertypes;
    if (allSupertypes == null) {
      allSupertypes = resolveAllSupertypes(this).collect(Collectors.toList());
      this.allSupertypes = allSupertypes;
    }
    return allSupertypes;
  }

  @Override
  public @Nullable JType asSupertype(JClass<?> base) {
    if (base == classifier) {
      return this;
    }
    for (var supertype : allSupertypes()) {
      var classifier = supertype.classifier();
      if (classifier instanceof JClass<?> && classifier.equals(base)) {
        return supertype;
      }
    }
    return null;
  }

  @Override
  public JType reify(JType reifiedType) {
    return null;
  }

  @Override
  public JType resolve() {
    var resolved = resolveOrNull();
    return resolved == null ? this : resolved;
  }

  private @Nullable JType resolveOrNull() {
    var classifier = this.classifier;
    if (classifier instanceof JTypeParameter) {
      var upperBound = (JTypeImpl) ((JTypeParameter) classifier).upperBound();
      classifier = upperBound.classifier();
      // TODO: Merge annotations
      return new JTypeImpl(classifier, upperBound.arguments, upperBound.nullability, upperBound.annotations);
    } else if (classifier instanceof JTypeIntersection) {
      var typeIntersection = (JTypeIntersection) classifier;
      var types = typeIntersection.types();
      List<JType> resolvedTypes = null;
      for (int i = 0; i < types.size(); i++) {
        var type = types.get(i);
        var resolved = ((JTypeImpl) type).resolveOrNull();
        if (resolved != null && resolved != type) {
          if (resolvedTypes == null) {
            resolvedTypes = new ArrayList<>(types.size());
            resolvedTypes.addAll(types.subList(0, i));
          }
          resolvedTypes.add(resolved);
        } else if (resolvedTypes != null) {
          resolvedTypes.add(type);
        }
      }
      return resolvedTypes == null ? null : intersectionOf(resolvedTypes, nullability);
    }
    List<JTypeProjection> resolvedArguments = null;
    for (int i = 0; i < arguments.size(); i++) {
      var argument = arguments.get(i);
      var argumentType = argument.type();
      JType resolved = null;
      if (argumentType != null) {
        resolved = ((JTypeImpl) argumentType).resolveOrNull();
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
    return resolvedArguments != null ? new JTypeImpl(classifier, resolvedArguments, nullability, annotations) : null;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj != null) {
      if (obj instanceof JTypeCapture<?>) {
        obj = ((JTypeCapture<?>) obj).type;
      }
      return obj instanceof JTypeImpl && equals((JTypeImpl) obj);
    }
    return false;
  }

  private boolean equals(JTypeImpl obj) {
    return Objects.equals(classifier, obj.classifier) && nullability == obj.nullability &&
        Objects.equals(annotations, obj.annotations) && Objects.equals(arguments, obj.arguments);
  }

  @Override
  public int hashCode() {
    int hash = this.hash;
    if (hash == 0) {
      hash = Objects.hash(classifier, nullability, annotations, arguments);
      this.hash = hash;
    }
    return hash;
  }

  @Override
  public String toString() {
    // TODO
    return javaType().toString();
  }
}
