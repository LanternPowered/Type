package org.lanternpowered.jtype;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;

final class JTypeContext {

  static final JTypeContext DEFAULT = new JTypeContext();

  @Nullable JFunctionExecutableImpl<?> function;
  boolean nullMarked;

  private enum NullMarkedState {
    MARKED,
    UNMARKED,
  }

  private static @Nullable NullMarkedState nullMarkedStateFromAnnotations(AnnotatedElement annotatedElement) {
    if (annotatedElement.isAnnotationPresent(NullUnmarked.class)) {
      return NullMarkedState.UNMARKED;
    }
    if (annotatedElement.isAnnotationPresent(NullMarked.class)) {
      return NullMarkedState.MARKED;
    }
    return null;
  }

  private static @Nullable NullMarkedState nullMarkedState(Class<?> type) {
    var state = nullMarkedStateFromAnnotations(type);
    if (state == null) {
      var enclosingClass = type.getEnclosingClass();
      if (enclosingClass != null) {
        state = nullMarkedState(enclosingClass);
      } else {
        var pkg = type.getPackage();
        if (pkg != null) {
          state = nullMarkedStateFromAnnotations(pkg);
        }
        if (state == null) {
          var module = type.getModule();
          if (module.isNamed()) {
            state = nullMarkedStateFromAnnotations(module);
          }
        }
      }
    }
    return state;
  }

  static boolean nullMarked(Executable executable) {
    var state = nullMarkedStateFromAnnotations(executable);
    if (state == null) {
      state = nullMarkedState(executable.getDeclaringClass());
    }
    return state == NullMarkedState.MARKED;
  }

  static boolean nullMarked(Class<?> type) {
    return nullMarkedState(type) == NullMarkedState.MARKED;
  }

  static boolean nullMarked(GenericDeclaration genericDeclaration) {
    if (genericDeclaration instanceof Executable) {
      return nullMarked((Executable) genericDeclaration);
    } else if (genericDeclaration instanceof Class<?>) {
      return nullMarked((Class<?>) genericDeclaration);
    }
    return false;
  }
}
