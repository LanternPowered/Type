package org.lanternpowered.jtype;

import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;

final class JTypeContext {

  static final JTypeContext DEFAULT = new JTypeContext();

  private static final boolean CHECKER_FRAMEWORK;

  static {
    var checkerFramework = false;
    try {
      var ignored = JTypeContext.class.getAnnotation(org.checkerframework.checker.nullness.qual.NonNull.class);
      checkerFramework = true;
    } catch (NoClassDefFoundError ignored) {
    }
    CHECKER_FRAMEWORK = checkerFramework;
  }

  @Nullable JFunctionExecutableImpl<?> function;
  @Nullable Nullability defaultNullability;

  private static @Nullable Nullability defaultNullabilityFromAnnotations(AnnotatedElement annotatedElement) {
    if (annotatedElement.isAnnotationPresent(NullUnmarked.class)) {
      return Nullability.UNKNOWN;
    }
    if (annotatedElement.isAnnotationPresent(NullMarked.class)) {
      return Nullability.NON_NULL;
    }
    if (CHECKER_FRAMEWORK) {
      return CheckerFramework.defaultNullabilityFromAnnotations(annotatedElement);
    }
    return null;
  }

  private static final class CheckerFramework {

    static @Nullable Nullability defaultNullabilityFromAnnotations(AnnotatedElement annotatedElement) {
      var defaultQualifier = annotatedElement.getAnnotation(DefaultQualifier.class);
      if (defaultQualifier != null) {
        return defaultNullability(defaultQualifier);
      }
      var defaultQualifierList = annotatedElement.getAnnotation(DefaultQualifier.List.class);
      if (defaultQualifierList != null) {
        for (var defaultQualifierEntry : defaultQualifierList.value()) {
          var nullability = defaultNullability(defaultQualifierEntry);
          if (nullability != null) {
            return nullability;
          }
        }
      }
      return null;
    }

    static @Nullable Nullability defaultNullability(DefaultQualifier defaultQualifier) {
      var locations = defaultQualifier.locations();
      // for now only support all locations
      var allLocations = false;
      for (var location : locations) {
        if (location == TypeUseLocation.ALL) {
          allLocations = true;
          break;
        }
      }
      if (allLocations) {
        var value = defaultQualifier.value();
        if (value == org.checkerframework.checker.nullness.qual.NonNull.class) {
          return Nullability.NON_NULL;
        } else if (value == org.checkerframework.checker.nullness.qual.Nullable.class) {
          return Nullability.NULLABLE;
        }
      }
      return null;
    }
  }

  private static @Nullable Nullability defaultNullability(Class<?> type) {
    var nullability = defaultNullabilityFromAnnotations(type);
    if (nullability == null) {
      var enclosingClass = type.getEnclosingClass();
      if (enclosingClass != null) {
        nullability = defaultNullability(enclosingClass);
      } else {
        var pkg = type.getPackage();
        if (pkg != null) {
          nullability = defaultNullabilityFromAnnotations(pkg);
        }
        if (nullability == null) {
          var module = type.getModule();
          if (module.isNamed()) {
            nullability = defaultNullabilityFromAnnotations(module);
          }
        }
      }
    }
    return nullability;
  }

  static @Nullable Nullability defaultNullability(Executable executable) {
    var state = defaultNullabilityFromAnnotations(executable);
    if (state == null) {
      state = defaultNullability(executable.getDeclaringClass());
    }
    return state;
  }

  static @Nullable Nullability defaultNullability(GenericDeclaration genericDeclaration) {
    if (genericDeclaration instanceof Executable) {
      return defaultNullability((Executable) genericDeclaration);
    } else if (genericDeclaration instanceof Class<?>) {
      return defaultNullability((Class<?>) genericDeclaration);
    }
    return null;
  }
}
