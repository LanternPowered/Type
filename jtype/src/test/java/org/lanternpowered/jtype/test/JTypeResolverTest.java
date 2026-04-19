/*
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.jtype.test;

import org.junit.jupiter.api.Test;
import org.lanternpowered.jtype.JFunction;
import org.lanternpowered.jtype.JType;
import org.lanternpowered.jtype.JTypeCapture;
import org.lanternpowered.jtype.JTypeResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class JTypeResolverTest {

  @Test
  public void resolveMethodReturnType_first() throws NoSuchMethodException {
    var function = JFunction.of(JTypeResolverTest.class.getMethod("first", List.class));
    var resolved = JTypeResolver.create()
        .where(function.parameters().getFirst(), new JTypeCapture<List<Apple>>() {})
        .resolve(function.returnType());
    assertEquals(new JTypeCapture<Apple>() {}, resolved);
  }

  @Test
  public void resolveParameterType_first() throws NoSuchMethodException {
    var function = JFunction.of(JTypeResolverTest.class.getMethod("first", List.class));
    var resolved = JTypeResolver.create()
        .where(function.typeParameters().getFirst(), new JTypeCapture<Apple>() {})
        .resolve(function.parameters().getFirst());
    assertEquals(new JTypeCapture<List<Apple>>() {}, resolved);
  }

  @Test
  public void resolveParameterTypeFromReturnType_first() throws NoSuchMethodException {
    var function = JFunction.of(JTypeResolverTest.class.getMethod("first", List.class));
    var resolved = JTypeResolver.create()
        .whereReturns(function, new JTypeCapture<Apple>() {})
        .resolve(function.parameters().getFirst());
    assertEquals(new JTypeCapture<List<Apple>>() {}, resolved);
  }

  public static <T extends Fruit> T first(List<T> list) {
    return list.getFirst();
  }

  @Test
  public void resolveMethodReturnType_distinct() throws NoSuchMethodException {
    var function = JFunction.of(JTypeResolverTest.class.getMethod("distinct", List.class));
    var resolved = JTypeResolver.create()
        .where(function.parameters().getFirst(), new JTypeCapture<ArrayList<Apple>>() {})
        .resolve(function.returnType());
    assertEquals(new JTypeCapture<Set<Apple>>() {}, resolved);
  }

  public static <T extends Fruit> Set<T> distinct(List<T> list) {
    return new HashSet<>(list);
  }

  @Test
  public void resolveReturnType_listGet() throws NoSuchMethodException {
    var function = JFunction.of(List.class.getMethod("get", int.class));
    var resolved = JTypeResolver.create()
        .whereReceiver(new JTypeCapture<ArrayList<Apple>>() {})
        .resolve(function.returnType());
    assertEquals(new JTypeCapture<Apple>() {}, resolved);
  }

  @Test
  public void resolveMethodReturnType_firstEnum() throws NoSuchMethodException {
    var function = JFunction.of(JTypeResolverTest.class.getMethod("firstEnum", List.class));
    var resolved = JTypeResolver.create()
        .where(function.parameters().getFirst(), new JTypeCapture<List<Dir>>() {})
        .resolve(function.returnType());
    assertEquals(new JTypeCapture<Dir>() {}, resolved);
  }

  public static <E extends Enum<E>> E firstEnum(List<E> list) {
    return list.getFirst();
  }

  @Test
  public void resolveMethodReturnType_pair() throws NoSuchMethodException {
    var function = JFunction.of(JTypeResolverTest.class.getMethod("pair", List.class, List.class));
    var resolved = JTypeResolver.create()
        .where(function.parameters().get(0), new JTypeCapture<List<Fruit>>() {})
        .where(function.parameters().get(1), new JTypeCapture<ArrayList<Dir>>() {})
        .resolve(function.returnType());
    assertEquals(new JTypeCapture<Map<Fruit, Dir>>() {}, resolved);
  }

  public static <A, B> Map<A, B> pair(List<A> a, List<B> b) {
    return Map.of();
  }

  @Test
  public void typeResolverFunction() {
    assertEquals(new JTypeCapture<Map<Fruit, Dir>>() {}, mapType(Fruit.class, Dir.class));
  }

  public <A, B> JType mapType(Class<A> key, Class<B> value) {
    return JTypeResolver.create()
        .where(new JTypeCapture<A>() {}, key)
        .where(new JTypeCapture<B>() {}, value)
        .resolve(new JTypeCapture<Map<A, B>>() {});
  }
}
