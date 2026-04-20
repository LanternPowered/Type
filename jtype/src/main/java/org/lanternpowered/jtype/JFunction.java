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

import static java.util.Objects.requireNonNull;

public interface JFunction<R extends @Nullable Object> extends JCallable<R> {

  static <R extends @Nullable Object> JFunction<R> of(Executable executable) {
    return JFunctionExecutableImpl.of(requireNonNull(executable, "executable"));
  }
}
