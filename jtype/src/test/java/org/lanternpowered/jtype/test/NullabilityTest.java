package org.lanternpowered.jtype.test;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.lanternpowered.jtype.JType;
import org.lanternpowered.jtype.JTypeCapture;
import org.lanternpowered.jtype.Nullability;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class NullabilityTest {

  @Test
  public void nullMarked() {
    var apple = new JTypeCapture<Apple>() {};
    // @NullMarked test package
    assertEquals(Nullability.NON_NULL, apple.nullability());
  }

  @Test
  public void nullUnmarked() {
    assertEquals(Nullability.UNKNOWN, UnmarkedTypes.apple.nullability());
    assertEquals(Nullability.NULLABLE, UnmarkedTypes.nullableApple.nullability());
    assertEquals(Nullability.NON_NULL, UnmarkedTypes.nonNullApple.nullability());
  }

  @Test
  public void nullable() {
    var apple = new JTypeCapture<@Nullable Apple>() {};
    assertEquals(Nullability.NULLABLE, apple.nullability());
  }

  @NullUnmarked
  private static final class UnmarkedTypes {
    static final JType apple = new JTypeCapture<Apple>() {};
    static final JType nullableApple = new JTypeCapture<@Nullable Apple>() {};
    static final JType nonNullApple = new JTypeCapture<@NonNull Apple>() {};
  }
}
