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

package org.bremersee.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The ip address matcher test.
 *
 * @author Christian Bremer
 */
class IpAddressMatcherTest {

  /**
   * Matches ip.
   */
  @Test
  void matchesIp() {
    IpAddressMatcher matcher = new IpAddressMatcher("192.168.1.23");
    assertTrue(matcher.matches("192.168.1.23"));
    assertFalse(matcher.matches("192.168.1.24"));
  }

  /**
   * Matches net.
   */
  @Test
  void matchesNet() {
    IpAddressMatcher matcher = new IpAddressMatcher("192.168.1.0/24");
    assertTrue(matcher.matches("192.168.1.23"));
    assertFalse(matcher.matches("192.168.2.24"));

    matcher = new IpAddressMatcher("192.168.0.0/16");
    assertTrue(matcher.matches("192.168.1.23"));
    assertTrue(matcher.matches("192.168.2.24"));
    assertFalse(matcher.matches("192.169.2.24"));
  }

}