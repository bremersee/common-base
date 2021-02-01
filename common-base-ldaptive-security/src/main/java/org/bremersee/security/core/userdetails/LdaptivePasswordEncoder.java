/*
 * Copyright 2021 the original author or authors.
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

package org.bremersee.security.core.userdetails;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * The ldaptive password encoder.
 *
 * @author Christian Bremer
 */
@ToString
@Slf4j
public class LdaptivePasswordEncoder implements PasswordEncoder {

  /**
   * Plain ldaptive password encoder.
   *
   * @return the ldaptive password encoder
   */
  public static LdaptivePasswordEncoder plain() {
    return new LdaptivePasswordEncoder("plain", null);
  }

  /**
   * Plain with no label ldaptive password encoder.
   *
   * @return the ldaptive password encoder
   */
  public static LdaptivePasswordEncoder plainWithNoLabel() {
    return new LdaptivePasswordEncoder(null, null);
  }

  /**
   * The constant delegate.
   */
  protected static final PasswordEncoder delegate = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Getter(value = AccessLevel.PROTECTED)
  private final String label;

  @Getter(value = AccessLevel.PROTECTED)
  private final String algorithm;

  /**
   * Instantiates a new ldaptive password encoder.
   */
  public LdaptivePasswordEncoder() {
    this("SHA", "SHA");
  }

  /**
   * Instantiates a new ldaptive password encoder.
   *
   * @param label the label
   * @param algorithm the algorithm
   */
  public LdaptivePasswordEncoder(String label, String algorithm) {
    this.label = label;
    this.algorithm = algorithm;
  }

  @Override
  public String encode(CharSequence rawPassword) {
    String raw = rawPassword != null ? rawPassword.toString() : "";
    StringBuilder sb = new StringBuilder();
    if (StringUtils.hasText(getLabel())) {
      sb.append('{').append(getLabel()).append('}');
    }
    if (!StringUtils.hasText(getAlgorithm()) || "plain".equalsIgnoreCase(getAlgorithm())) {
      sb.append(rawPassword);
    } else {
      sb.append(encrypt(raw));
    }
    return sb.toString();
  }

  private String encrypt(String raw) {
    try {
      final MessageDigest md = MessageDigest.getInstance(getAlgorithm());
      md.update(raw.getBytes(StandardCharsets.UTF_8));
      byte[] hash = md.digest();
      return new String(Base64.getEncoder().encode(hash), StandardCharsets.UTF_8);

    } catch (NoSuchAlgorithmException e) {
      throw ServiceException.internalServerError("Algorithm '" + getAlgorithm() + "' was not found.", e);
    }
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    String raw = rawPassword != null ? rawPassword.toString() : "";
    String enc = encodedPassword != null ? encodedPassword : "";
    int index = enc.indexOf('}');
    if (enc.startsWith("{") && index > 0) {
      String foundLabel = enc.substring(1, index);
      if ("plain".equalsIgnoreCase(foundLabel)) {
        return enc.equals(LdaptivePasswordEncoder.plain().encode(raw));
      } else if (foundLabel.equals(getLabel())) {
        return enc.equals(encode(raw));
      } else {
        return delegate.matches(raw, enc);
      }
    } else if (!StringUtils.hasText(getLabel())) {
      return StringUtils.hasText(getAlgorithm())
          ? enc.equals(encode(raw))
          : enc.equals(LdaptivePasswordEncoder.plainWithNoLabel().encode(raw));
    }
    return false;
  }

}
