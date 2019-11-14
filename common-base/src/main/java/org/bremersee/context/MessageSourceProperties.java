/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.context;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The message source properties.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties("bremersee.messages")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MessageSourceProperties {

  private List<String> baseNames = new ArrayList<>();

  private Map<String, String> fileEncodings = new HashMap<>();

  private boolean concurrentRefresh = true;

  private String defaultEncoding = StandardCharsets.UTF_8.name();

  private boolean fallbackToSystemLocale = true;

  private int cacheSeconds = -1;

  private boolean useCodeAsDefaultMessage = false;

}
