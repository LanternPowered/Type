/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import org.jspecify.annotations.Nullable;

import java.util.List;

public interface JCallable<R extends @Nullable Object> extends JAnnotatedElement {

  String name();

  List<JParameter> parameters();

  JType returnType();

  List<JTypeParameter> typeParameters();

  R call(@Nullable Object... args);
}
