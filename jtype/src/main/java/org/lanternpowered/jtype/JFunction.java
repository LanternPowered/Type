/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.reflect.Executable;

import static java.util.Objects.requireNonNull;

public interface JFunction<R> extends JCallable<R> {

  static <R> JFunction<R> of(Executable executable) {
    return JFunctionExecutableImpl.of(requireNonNull(executable, "executable"));
  }
}
