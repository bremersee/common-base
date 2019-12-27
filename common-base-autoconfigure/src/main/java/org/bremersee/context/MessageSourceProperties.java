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
@ConfigurationProperties("bremersee.messages")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MessageSourceProperties {

  /**
   * Set whether to always apply the {@code MessageFormat} rules, parsing even messages without
   * arguments.
   *
   * <p>Default is "false": Messages without arguments are by default
   * returned as-is, without parsing them through MessageFormat. Set this to "true" to enforce
   * MessageFormat for all messages, expecting all message texts to be written with MessageFormat
   * escaping.
   *
   * <p>For example, MessageFormat expects a single quote to be escaped
   * as "''". If your message texts are all written with such escaping, even when not defining
   * argument placeholders, you need to set this flag to "true". Else, only message texts with
   * actual arguments are supposed to be written with MessageFormat escaping.
   *
   * @see java.text.MessageFormat
   */
  private boolean alwaysUseMessageFormat = false; // from MessageSourceSupport

  /**
   * Set whether to use the message code as default message instead of throwing a
   * NoSuchMessageException. Useful for development and debugging. Default is "false".
   */
  private boolean useCodeAsDefaultMessage = false; // from AbstractMessageSource

  /**
   * Set an list of basenames, each following the basic ResourceBundle convention of not specifying
   * file extension or language codes. The resource location format is up to the specific {@code
   * MessageSource} implementation.
   *
   * <p>Regular and XMl properties files are supported: e.g. "messages" will find
   * a "messages.properties", "messages_en.properties" etc arrangement as well as "messages.xml",
   * "messages_en.xml" etc.
   *
   * <p>The associated resource bundles will be checked sequentially when resolving
   * a message code. Note that message definitions in a <i>previous</i> resource bundle will
   * override ones in a later bundle, due to the sequential lookup.
   */
  private List<String> baseNames = new ArrayList<>(); // from AbstractResourceBasedMessageSource

  /**
   * Set the number of seconds to cache loaded properties files.
   * <ul>
   * <li>Default is "-1", indicating to cache forever (just like
   * {@code java.util.ResourceBundle}).
   * <li>A positive number will cache loaded properties files for the given
   * number of seconds. This is essentially the interval between refresh checks.
   * Note that a refresh attempt will first check the last-modified timestamp
   * of the file before actually reloading it; so if files don't change, this
   * interval can be set rather low, as refresh attempts will not actually reload.
   * <li>A value of "0" will check the last-modified timestamp of the file on
   * every message access. <b>Do not use this in a production environment!</b>
   * </ul>
   */
  private int cacheSeconds = -1; // from AbstractResourceBasedMessageSource

  /**
   * Set the default charset to use for parsing properties files. Used if no file-specific charset
   * is specified for a file.
   *
   * <p>The effective default is the {@code java.util.Properties}
   * default encoding: ISO-8859-1. A {@code null} value indicates the platform default encoding.
   *
   * <p>Only applies to classic properties files, not to XML files.
   */
  private String defaultEncoding = StandardCharsets.UTF_8.name(); // from AbstractResourceBasedM.

  /**
   * Set whether to fall back to the system Locale if no files for a specific Locale have been
   * found. Default is "true"; if this is turned off, the only fallback will be the default file
   * (e.g. "messages.properties" for basename "messages").
   *
   * <p>Falling back to the system Locale is the default behavior of
   * {@code java.util.ResourceBundle}. However, this is often not desirable in an application server
   * environment, where the system Locale is not relevant to the application at all: set this flag
   * to "false" in such a scenario.
   */
  private boolean fallbackToSystemLocale = true;

  /**
   * Specify a default Locale to fall back to, as an alternative to falling back to the system
   * Locale.
   *
   * <p>Default is to fall back to the system Locale. You may override this with
   * a locally specified default Locale here, or enforce no fallback locale at all through
   * disabling.
   */
  private String defaultLocale;

  /**
   * Specifies whether the resource bundles are reloadable or not.
   */
  private boolean useReloadableMessageSource = false;

  /**
   * Specify whether to allow for concurrent refresh behavior, i.e. one thread locked in a refresh
   * attempt for a specific cached properties file whereas other threads keep returning the old
   * properties for the time being, until the refresh attempt has completed.
   *
   * <p>Default is "true": this behavior is new as of Spring Framework 4.1,
   * minimizing contention between threads. If you prefer the old behavior, i.e. to fully block on
   * refresh, switch this flag to "false".
   */
  private boolean concurrentRefresh = true; // ReloadableResourceBundleMessageSource

  /**
   * Set per-file charsets to use for parsing properties files.
   *
   * <p>Only applies to classic properties files, not to XML files.
   */
  private Map<String, String> fileEncodings = new HashMap<>(); // ReloadableResourceBundleMessageS.

}
