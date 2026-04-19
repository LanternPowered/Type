/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.util.List;

/**
 * Represents an intersection type. For example the part {@code A & B} in {@code ? extends A & B}.
 */
public interface JTypeIntersection extends JClassifier {

  /**
   * The types that are part of the intersection.
   */
  List<JType> types();
}
