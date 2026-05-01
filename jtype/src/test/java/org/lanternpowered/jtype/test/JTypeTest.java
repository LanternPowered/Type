/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.lanternpowered.jtype.JClass;
import org.lanternpowered.jtype.JType;
import org.lanternpowered.jtype.JTypeCapture;
import org.lanternpowered.jtype.JTypeIntersection;
import org.lanternpowered.jtype.Nullability;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class JTypeTest {

  @Test
  public void supertype() {
    var apple = new JTypeCapture<Apple>() {};
    assertEquals(apple.classifier(), JClass.of(Apple.class));
    assertEquals(apple.supertypes().getFirst().javaType(), Apple.class.getAnnotatedSuperclass());
    assertEquals(apple.supertypes().getFirst(), JType.nonNullOf(Apple.class.getAnnotatedSuperclass()));
    assertEquals(apple.supertypes().getFirst().classifier(), JClass.of(Fruit.class));
    var appleArrayList = new JTypeCapture<ArrayList<Apple>>() {};
    var appleList = new JTypeCapture<List<Apple>>() {};
    var asAppleList = appleArrayList.asSupertype(List.class);
    assertNotNull(asAppleList);
    assertEquals(appleList, asAppleList);
    assertEquals(JType.nonNullOf(RandomAccess.class), appleArrayList.asSupertype(RandomAccess.class));
    var asAbstractAppleList = appleArrayList.asSupertype(AbstractList.class);
    assertNotNull(asAbstractAppleList);
    assertEquals(new JTypeCapture<AbstractList<Apple>>() {}, asAbstractAppleList);
    assertEquals("java.util.List<org.lanternpowered.jtype.test.Apple>", appleList.toString());
    assertEquals("java.util.List<org.lanternpowered.jtype.test.Apple>", asAppleList.toString());
    assertEquals("java.util.ArrayList<org.lanternpowered.jtype.test.Apple>", appleArrayList.toString());
  }

  @Test
  public <T extends Apple & Red> void intersectionType() {
    var redApple = new JTypeCapture<T>() {}.resolve();
    assertInstanceOf(JTypeIntersection.class, redApple.classifier());
    var typeIntersection = (JTypeIntersection) redApple.classifier();
    var types = typeIntersection.types();
    assertEquals(2, types.size());
    assertEquals(JType.nonNullOf(Apple.class), types.get(0));
    assertEquals(JType.nonNullOf(Red.class), types.get(1));
  }

  @Test
  public void arrayOfNullableStrings() {
    var type = new JTypeCapture<@Nullable String[]>() {};
    assertEquals(Nullability.NON_NULL, type.nullability());
    assertEquals(1, type.arguments().size());
    assertEquals(new JTypeCapture<@Nullable String>() {}, type.arguments().getFirst().type());
    assertEquals(JClass.of(Object[].class), type.classifier());
    assertEquals(JClass.of(String[].class), type.rawType());
  }

  @Test
  public void nullableArrayOfStrings() {
    var type = new JTypeCapture<String @Nullable []>() {};
    assertEquals(Nullability.NULLABLE, type.nullability());
    assertEquals(1, type.arguments().size());
    assertEquals(new JTypeCapture<String>() {}, type.arguments().getFirst().type());
    assertEquals(JClass.of(Object[].class), type.classifier());
    assertEquals(JClass.of(String[].class), type.rawType());
  }

  @Test
  public void nullableArrayOfNullableStrings() {
    var type = new JTypeCapture<@Nullable String @Nullable []>() {};
    assertEquals(Nullability.NULLABLE, type.nullability());
    assertEquals(1, type.arguments().size());
    assertEquals(new JTypeCapture<@Nullable String>() {}, type.arguments().getFirst().type());
    assertEquals(JClass.of(Object[].class), type.classifier());
    assertEquals(JClass.of(String[].class), type.rawType());
  }

  @Test
  public void genericArrayOfStrings() {
    var type = genericArrayOfStringsType();
    assertEquals(Nullability.NON_NULL, type.nullability());
    assertEquals(1, type.arguments().size());
    assertEquals(new JTypeCapture<String>() {}, type.arguments().getFirst().type());
    assertEquals(JClass.of(Object[].class), type.classifier());
    assertEquals(JClass.of(Object[].class), type.rawType());
  }

  private static <T extends String> JType genericArrayOfStringsType() {
    return new JTypeCapture<T[]>() {}.resolve();
  }

  @Test
  public void genericArrayOfNullableStrings() {
    var type = genericArrayOfNullableStringsType();
    assertEquals(Nullability.NON_NULL, type.nullability());
    assertEquals(1, type.arguments().size());
    assertEquals(new JTypeCapture<@Nullable String>() {}, type.arguments().getFirst().type());
    assertEquals(JClass.of(Object[].class), type.classifier());
    assertEquals(JClass.of(Object[].class), type.rawType());
  }

  @NullUnmarked
  private static <T extends @Nullable String> @NonNull JType genericArrayOfNullableStringsType() {
    return new JTypeCapture<T @NonNull []>() {}.resolve();
  }

  @Test
  public void genericArrayOfNullableStrings_nullableT() {
    var type = genericArrayOfNullableStringsType_nullableT();
    assertEquals(Nullability.NON_NULL, type.nullability());
    assertEquals(1, type.arguments().size());
    assertEquals(new JTypeCapture<@Nullable String>() {}, type.arguments().getFirst().type());
    assertEquals(JClass.of(Object[].class), type.classifier());
    assertEquals(JClass.of(Object[].class), type.rawType());
  }

  private static <T extends String> JType genericArrayOfNullableStringsType_nullableT() {
    return new JTypeCapture<@Nullable T []>() {}.resolve();
  }
}
