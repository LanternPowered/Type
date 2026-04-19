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
