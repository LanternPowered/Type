/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test;

import org.junit.jupiter.api.Test;
import org.lanternpowered.jtype.JClass;
import org.lanternpowered.jtype.JType;
import org.lanternpowered.jtype.JTypeCapture;
import org.lanternpowered.jtype.JTypeIntersection;

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
    assertEquals(apple.supertypes().getFirst(), JType.of(Apple.class.getAnnotatedSuperclass()));
    assertEquals(apple.supertypes().getFirst().classifier(), JClass.of(Fruit.class));
    var appleArrayList = new JTypeCapture<ArrayList<Apple>>() {};
    var appleList = new JTypeCapture<List<Apple>>() {};
    var asAppleList = appleArrayList.asSupertype(List.class);
    assertNotNull(asAppleList);
    assertEquals(appleList, asAppleList);
    assertEquals(JType.of(RandomAccess.class), appleArrayList.asSupertype(RandomAccess.class));
    assertEquals(new JTypeCapture<AbstractList<Apple>>() {}, appleArrayList.asSupertype(AbstractList.class));
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
    assertEquals(JType.of(Apple.class), types.get(0));
    assertEquals(JType.of(Red.class), types.get(1));
  }
}
