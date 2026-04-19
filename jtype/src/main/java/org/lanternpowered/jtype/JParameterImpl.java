/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype;

import java.lang.reflect.Executable;
import java.util.Objects;

final class JParameterImpl implements JParameter {

  private final int index;
  private final String name;
  private final JType type;
  final Executable executable;

  JParameterImpl(int index, String name, JType type, Executable executable) {
    this.index = index;
    this.name = name;
    this.type = type;
    this.executable = executable;
  }

  @Override
  public int index() {
    return index;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public JType type() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JParameterImpl)) {
      return false;
    }
    var other = (JParameterImpl) obj;
    return Objects.equals(executable, other.executable) && index == other.index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(executable, index);
  }

  @Override
  public String toString() {
    return type + " " + name;
  }
}
