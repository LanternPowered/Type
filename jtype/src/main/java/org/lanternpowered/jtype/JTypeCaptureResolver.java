/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.reflect.AnnotatedParameterizedType;

final class JTypeCaptureResolver {

  static JType resolve(JTypeCapture<?> capture) {
    var rawType = capture.getClass();
    if (rawType.getSuperclass() != JTypeCapture.class) {
      throw new IllegalStateException("Only direct sub classes of JTypeCapture are allowed.");
    }
    return JType.of(((AnnotatedParameterizedType) rawType.getAnnotatedSuperclass()).getAnnotatedActualTypeArguments()[0]);
  }
}
