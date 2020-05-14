/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

/**
 * The ordered proxy test.
 *
 * @author Christian Bremer
 */
class OrderedProxyTest {

  /**
   * Create with target.
   */
  @Test
  void createWithTarget() {
    Target target = new Target("Anna");
    Ordered proxy = OrderedProxy.create(target, 123);
    assertNotNull(proxy);
    assertEquals(target.toString(), proxy.toString());
    assertEquals(123, proxy.getOrder());
  }

  /**
   * Create with say hello target.
   */
  @Test
  void createWithSayHelloTarget() {
    SayHelloTarget target = new SayHelloTarget("Anna");
    SayHello proxy = OrderedProxy.create(target, 123);
    assertNotNull(proxy);
    assertEquals("Hello Anna", proxy.sayHello());
    assertEquals(target.toString(), proxy.toString());
    assertTrue(proxy instanceof Ordered);
    Ordered ordered = (Ordered) proxy;
    assertEquals(123, ordered.getOrder());
  }

  /**
   * Create with ordered target and same values.
   */
  @Test
  void createWithOrderedTargetAndSameValues() {
    OrderedTarget target = new OrderedTarget(123);
    Ordered proxy = OrderedProxy.create(target, 123);
    assertNotNull(proxy);
    assertEquals(target.toString(), proxy.toString());
    assertEquals(target.hashCode(), proxy.hashCode());
    assertEquals(target, proxy);
    assertEquals(123, proxy.getOrder());
  }

  /**
   * Create with ordered target and different values.
   */
  @Test
  void createWithOrderedTargetAndDifferentValues() {
    OrderedTarget target = new OrderedTarget(124);
    Ordered proxy = OrderedProxy.create(target, 123);
    assertNotNull(proxy);
    assertEquals(target.toString(), proxy.toString());
    assertEquals(123, proxy.getOrder());
  }

  private interface SayHello {

    /**
     * Say hello string.
     *
     * @return the string
     */
    String sayHello();
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  private static class Target {

    private final String name;
  }

  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  private static class SayHelloTarget implements SayHello {

    private final String name;

    @Override
    public String sayHello() {
      return "Hello " + name;
    }
  }

  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  private static class OrderedTarget implements Ordered {

    private final int value;

    @Override
    public int getOrder() {
      return value;
    }
  }
}