/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

public enum Nullability {
  /**
   * The type is nullable.
   */
  NULLABLE,
  /**
   * The type will never be null.
   */
  NON_NULL,
  /**
   * The nullability of the type is not defined explicitly.
   */
  UNKNOWN,
}
