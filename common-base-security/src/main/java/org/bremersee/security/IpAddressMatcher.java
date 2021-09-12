/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Matches a request based on IP Address or subnet mask matching against the remote address.
 *
 * <p>Both IPv6 and IPv4 addresses are supported, but a matcher which is configured with an IPv4
 * address will never match a request which returns an IPv6 address, and vice-versa.
 *
 * <p>The original Spring {@code org.springframework.security.web.util.matcher.IpAddressMatcher}
 * imports {@code javax.servlet.http.HttpServletRequest}, that is not available in a reactive environment. Here is the
 * method {@code boolean matches(HttpServletRequest request)} skipped and ths class does not implements {@code
 * org.springframework.security.web.util.matcher.RequestMatcher},
 *
 * @author Luke Taylor
 */
public class IpAddressMatcher {

  private final int numMaskBits;

  private final InetAddress requiredAddress;

  /**
   * Takes a specific IP address or a range specified using the IP/Netmask (e.g. 192.168.1.0/24 or 202.24.0.0/14).
   *
   * @param ipAddress the address or range of addresses from which the request must come.
   */
  public IpAddressMatcher(String ipAddress) {

    if (ipAddress.indexOf('/') > 0) {
      String[] addressAndMask = StringUtils.split(ipAddress, "/");
      //noinspection ConstantConditions
      ipAddress = addressAndMask[0];
      numMaskBits = Integer.parseInt(addressAndMask[1]);
    } else {
      numMaskBits = -1;
    }
    requiredAddress = parseAddress(ipAddress);
    Assert.isTrue(requiredAddress.getAddress().length * 8 >= numMaskBits,
        String.format("IP address %s is too short for bitmask of length %d",
            ipAddress, numMaskBits));
  }

  /**
   * Matches an IPv6 and IPv4 address.
   *
   * @param address the address
   * @return the {@code true} id the given address matches an IPv6 and IPv4 address, otherwise {@code false}
   */
  public boolean matches(String address) {
    InetAddress remoteAddress = parseAddress(address);

    if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
      return false;
    }

    if (numMaskBits < 0) {
      return remoteAddress.equals(requiredAddress);
    }

    byte[] remAddr = remoteAddress.getAddress();
    byte[] reqAddr = requiredAddress.getAddress();

    int numMaskFullBytes = numMaskBits / 8;
    byte finalByte = (byte) (0xFF00 >> (numMaskBits & 0x07));

    // System.out.println("Mask is " + new sun.misc.HexDumpEncoder().encode(mask));

    for (int i = 0; i < numMaskFullBytes; i++) {
      if (remAddr[i] != reqAddr[i]) {
        return false;
      }
    }

    if (finalByte != 0) {
      return (remAddr[numMaskFullBytes] & finalByte) == (reqAddr[numMaskFullBytes] & finalByte);
    }

    return true;
  }

  private InetAddress parseAddress(String address) {
    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Failed to parse address" + address, e);
    }
  }
}
