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

package org.bremersee.data.ldaptive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyRequest;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.io.ValueTranscoder;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The ldap entry mapper.
 *
 * @param <T> the type of the domain object
 * @author Christian Bremer
 */
@Validated
public interface LdaptiveEntryMapper<T> extends LdapEntryMapper<T> {

  /**
   * Map a ldap entry into a domain object.
   *
   * @param ldapEntry the ldap entry
   * @return the domain object
   */
  @Nullable
  T map(@Nullable LdapEntry ldapEntry);

  @Override
  default void map(T source, LdapEntry destination) {
    mapAndComputeModifications(source, destination);
  }

  /**
   * Map and compute attribute modifications (see {@link LdapEntry#computeModifications(LdapEntry,
   * LdapEntry)}).
   *
   * @param source      the source (domain object)
   * @param destination the destination (ldap entry)
   * @return the attribute modifications
   */
  AttributeModification[] mapAndComputeModifications(
      @NotNull T source,
      @NotNull LdapEntry destination);

  /**
   * Map and compute modify request.
   *
   * @param source      the source (domain object)
   * @param destination the destination (ldap entry)
   * @return the modify request
   */
  default ModifyRequest mapAndComputeModifyRequest(
      @NotNull T source,
      @NotNull LdapEntry destination) {
    return new ModifyRequest(destination.getDn(), mapAndComputeModifications(source, destination));
  }

  static <T> T getAttributeValue(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder,
      final T defaultValue) {
    final LdapAttribute attr = ldapEntry == null ? null : ldapEntry.getAttribute(name);
    final T value = attr != null ? attr.getValue(valueTranscoder) : null;
    return value != null ? value : defaultValue;
  }

  static <T> Collection<T> getAttributeValues(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder) {
    final LdapAttribute attr = ldapEntry == null ? null : ldapEntry.getAttribute(name);
    final Collection<T> values = attr != null ? attr.getValues(valueTranscoder) : null;
    return values != null ? values : new ArrayList<>();
  }

  static <T> Set<T> getAttributeValuesAsSet(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder) {
    return new LinkedHashSet<>(getAttributeValues(ldapEntry, name, valueTranscoder));
  }

  static <T> List<T> getAttributeValuesAsList(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder) {
    return new ArrayList<>(getAttributeValues(ldapEntry, name, valueTranscoder));
  }

  /**
   * Sets a single attribute value or removes the attribute when the given value is {@code null}.
   *
   * @param <T>             the type of the domain object
   * @param ldapEntry       the ldap entry
   * @param name            the attribute name
   * @param value           the attribute value
   * @param isBinary        specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder (can be null if value is also null)
   * @param modifications   the list of modifications
   */
  static <T> void setAttribute(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final T value,
      final boolean isBinary,
      final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {

    LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null && value != null) {
      addAttribute(ldapEntry, name, value, isBinary, valueTranscoder, modifications);
    } else if (attr != null) {
      if (value == null) {
        removeAttribute(ldapEntry, name, null, valueTranscoder, modifications);
      } else {
        final LdapAttribute newAttr = attr.isBinary()
            ? new LdapAttribute(name, valueTranscoder.encodeBinaryValue(value))
            : new LdapAttribute(name, valueTranscoder.encodeStringValue(value));
        ldapEntry.removeAttribute(attr);
        ldapEntry.addAttribute(newAttr);
        modifications.add(
            new AttributeModification(
                AttributeModificationType.REPLACE,
                newAttr));
      }
    }
  }

  /**
   * Sets attribute values.
   *
   * @param <T>             the type of the domain object
   * @param ldapEntry       the ldap entry
   * @param name            the attribute name
   * @param values          the values of the attribute
   * @param isBinary        specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder (can be null if values is also null)
   * @param modifications   the list of modifications
   */
  static <T> void setAttributes(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final Collection<T> values,
      final boolean isBinary,
      final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {

    // Can I use replace here? That would keep the order?
    LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null && values != null) {
      for (final T value : values) {
        addAttribute(ldapEntry, name, value, isBinary, valueTranscoder, modifications);
      }
    } else if (attr != null) {
      if (values == null || values.isEmpty()) {
        removeAttribute(ldapEntry, name, null, valueTranscoder, modifications);
      } else {
        final LdapAttribute newAttr = new LdapAttribute(name);
        newAttr.addValues(valueTranscoder, values);
        ldapEntry.removeAttribute(attr);
        ldapEntry.addAttribute(newAttr);
        modifications.add(
            new AttributeModification(
                AttributeModificationType.REPLACE,
                newAttr));
      }
    }
    /*
    final LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null && values != null) {
      for (final T value : values) {
        addAttribute(ldapEntry, name, value, isBinary, valueTranscoder, modifications);
      }
    } else if (attr != null) {
      if (values == null || values.isEmpty()) {
        removeAttribute(ldapEntry, name, null, valueTranscoder, modifications);
      } else {
        final Set<String> oldValueSet = isBinary
            ? attr.getBinaryValues().stream()
            .map(value -> Base64.getEncoder().encodeToString(value))
            .collect(Collectors.toSet())
            : new HashSet<>(attr.getStringValues());
        final List<String> newValues = values.stream()
            .map(value -> isBinary
                ? Base64.getEncoder().encodeToString(valueTranscoder.encodeBinaryValue(value))
                : valueTranscoder.encodeStringValue(value))
            .filter(newValue -> !oldValueSet.contains(newValue))
            .collect(Collectors.toList());
        final Set<String> newValueSet = new HashSet<>(newValues);
        for (final String oldValue : oldValueSet) {
          if (!newValueSet.contains(oldValue)) {
            final LdapAttribute oldAttr = isBinary
                ? new LdapAttribute(name, Base64.getDecoder().decode(oldValue))
                : new LdapAttribute(name, oldValue);
            ldapEntry.removeAttribute(oldAttr);
            modifications.add(
                new AttributeModification(
                    AttributeModificationType.REMOVE,
                    oldAttr));
          }
        }
        for (final String newValue : newValues) {
          final LdapAttribute newAttr = isBinary
              ? new LdapAttribute(name, Base64.getDecoder().decode(newValue))
              : new LdapAttribute(name, newValue);
          ldapEntry.addAttribute(newAttr);
          modifications.add(
              new AttributeModification(
                  AttributeModificationType.ADD,
                  newAttr));
        }
      }
    }
    */
  }

  /**
   * Adds an attribute to the ldap entry.
   *
   * @param <T>             the type of the domain object
   * @param ldapEntry       the ldap entry
   * @param name            the attribute name
   * @param value           the attribute value
   * @param isBinary        specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder
   * @param modifications   the list of modifications
   */
  static <T> void addAttribute(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @NotNull final T value,
      final boolean isBinary,
      @NotNull final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {
    final LdapAttribute attr = isBinary
        ? new LdapAttribute(name, valueTranscoder.encodeBinaryValue(value))
        : new LdapAttribute(name, valueTranscoder.encodeStringValue(value));
    ldapEntry.addAttribute(attr);
    modifications.add(
        new AttributeModification(
            AttributeModificationType.ADD,
            attr));
  }

  /**
   * Removes an attribute from the ldap entry.
   *
   * @param <T>             the type of the domain object
   * @param ldapEntry       the ldap entry
   * @param name            the name
   * @param value           the value
   * @param valueTranscoder the value transcoder
   * @param modifications   the modifications
   */
  static <T> void removeAttribute(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final T value,
      final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {
    LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null) {
      return;
    }
    boolean isBinary = attr.isBinary();
    if (value == null) {
      ldapEntry.removeAttribute(name);
    } else if (isBinary) {
      attr = new LdapAttribute(name, valueTranscoder.encodeBinaryValue(value));
      ldapEntry.removeAttribute(attr);
    } else {
      attr = new LdapAttribute(name, valueTranscoder.encodeStringValue(value));
      ldapEntry.removeAttribute(attr);
    }
    modifications.add(
        new AttributeModification(
            AttributeModificationType.REMOVE,
            attr));
  }

  /**
   * Create dn string.
   *
   * @param rdn      the rdn
   * @param rdnValue the rdn value
   * @param baseDn   the base dn
   * @return the string
   */
  static String createDn(
      @NotNull final String rdn,
      @NotNull final String rdnValue,
      @NotNull final String baseDn) {
    return rdn + "=" + rdnValue + "," + baseDn;
  }

  /**
   * Gets rdn.
   *
   * @param dn the dn
   * @return the rdn
   */
  static String getRdn(final String dn) {
    if (dn == null) {
      return null;
    }
    int start = dn.indexOf('=');
    if (start < 0) {
      return dn;
    }
    int end = dn.indexOf(',', start);
    if (end < 0) {
      return dn.substring(start + 1).trim();
    }
    return dn.substring(start + 1, end).trim();
  }

}
