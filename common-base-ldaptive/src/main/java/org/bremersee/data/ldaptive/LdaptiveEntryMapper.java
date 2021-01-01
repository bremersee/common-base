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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModification.Type;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyRequest;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.transcode.ValueTranscoder;
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
   * Get object classes of the ldap entry. The object classes are only required, if a new ldap entry should be
   * persisted.
   *
   * @return the object classes of the ldap entry
   */
  String[] getObjectClasses();

  @Override
  String mapDn(T domainObject);

  /**
   * Map a ldap entry into a domain object.
   *
   * @param ldapEntry the ldap entry
   * @return the domain object
   */
  @Nullable
  T map(@Nullable LdapEntry ldapEntry);

  @Override
  void map(LdapEntry source, T destination);

  @Override
  default void map(T source, LdapEntry destination) {
    mapAndComputeModifications(source, destination);
  }

  /**
   * Map and compute attribute modifications (see {@link LdapEntry#computeModifications(LdapEntry, LdapEntry)}**).
   *
   * @param source the source (domain object)
   * @param destination the destination (ldap entry)
   * @return the attribute modifications
   */
  AttributeModification[] mapAndComputeModifications(
      @NotNull T source,
      @NotNull LdapEntry destination);

  /**
   * Map and compute modify request.
   *
   * @param source the source (domain object)
   * @param destination the destination (ldap entry)
   * @return the modify request
   */
  default ModifyRequest mapAndComputeModifyRequest(
      @NotNull T source,
      @NotNull LdapEntry destination) {
    return new ModifyRequest(destination.getDn(), mapAndComputeModifications(source, destination));
  }

  /**
   * Gets attribute value.
   *
   * @param <T> the type parameter
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param valueTranscoder the value transcoder
   * @param defaultValue the default value
   * @return the attribute value
   */
  static <T> T getAttributeValue(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder,
      final T defaultValue) {
    final LdapAttribute attr = ldapEntry == null ? null : ldapEntry.getAttribute(name);
    final T value = attr != null ? attr.getValue(valueTranscoder.decoder()) : null;
    return value != null ? value : defaultValue;
  }

  /**
   * Gets attribute values.
   *
   * @param <T> the type parameter
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param valueTranscoder the value transcoder
   * @return the attribute values
   */
  static <T> Collection<T> getAttributeValues(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder) {
    final LdapAttribute attr = ldapEntry == null ? null : ldapEntry.getAttribute(name);
    final Collection<T> values = attr != null ? attr.getValues(valueTranscoder.decoder()) : null;
    return values != null ? values : new ArrayList<>();
  }

  /**
   * Gets attribute values as set.
   *
   * @param <T> the type parameter
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param valueTranscoder the value transcoder
   * @return the attribute values as set
   */
  static <T> Set<T> getAttributeValuesAsSet(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder) {
    return new LinkedHashSet<>(getAttributeValues(ldapEntry, name, valueTranscoder));
  }

  /**
   * Gets attribute values as list.
   *
   * @param <T> the type parameter
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param valueTranscoder the value transcoder
   * @return the attribute values as list
   */
  static <T> List<T> getAttributeValuesAsList(
      @Nullable final LdapEntry ldapEntry,
      @NotNull final String name,
      final ValueTranscoder<T> valueTranscoder) {
    return new ArrayList<>(getAttributeValues(ldapEntry, name, valueTranscoder));
  }

  /**
   * Replaces the value of the attribute with the specified value.
   *
   * @param <T> the type of the domain object
   * @param ldapEntry the ldap entry
   * @param name the attribute name
   * @param value the attribute value
   * @param isBinary specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder (can be null if value is also null)
   * @param modifications the list of modifications
   */
  static <T> void setAttribute(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final T value,
      final boolean isBinary,
      final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {

    setAttributes(
        ldapEntry,
        name,
        value != null ? Collections.singleton(value) : null,
        isBinary,
        valueTranscoder,
        modifications);
  }

  /**
   * Replaces the values of the attribute with the specified values.
   *
   * @param <T> the type of the domain object
   * @param ldapEntry the ldap entry
   * @param name the attribute name
   * @param values the values of the attribute
   * @param isBinary specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder (can be null if values is also null)
   * @param modifications the list of modifications
   */
  static <T> void setAttributes(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final Collection<T> values,
      final boolean isBinary,
      final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {

    final Collection<T> realValues = values == null ? null : values.stream()
        .filter(value -> {
          if (value instanceof CharSequence) {
            return ((CharSequence) value).length() > 0;
          }
          return value != null;
        })
        .collect(Collectors.toList());
    LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null && realValues != null && !realValues.isEmpty()) {
      addAttributes(ldapEntry, name, realValues, isBinary, valueTranscoder, modifications);
    } else if (attr != null) {
      if (realValues == null || realValues.isEmpty()) {
        ldapEntry.removeAttribute(name);
        modifications.add(
            new AttributeModification(
                Type.DELETE,
                attr));
      } else if (!new ArrayList<>(realValues)
          .equals(new ArrayList<>(attr.getValues(valueTranscoder.decoder())))) {
        final LdapAttribute newAttr = new LdapAttribute();
        newAttr.setBinary(isBinary);
        newAttr.setName(name);
        newAttr.addValues(valueTranscoder.encoder(), realValues);
        ldapEntry.addAttributes(newAttr);
        modifications.add(
            new AttributeModification(
                Type.REPLACE,
                newAttr));
      }
    }
  }

  /**
   * Adds the specified value to the attribute with the specified name.
   *
   * @param <T> the type of the domain object
   * @param ldapEntry the ldap entry
   * @param name the attribute name
   * @param value the attribute value
   * @param isBinary specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder
   * @param modifications the list of modifications
   */
  static <T> void addAttribute(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final T value,
      final boolean isBinary,
      @NotNull final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {
    addAttributes(
        ldapEntry,
        name,
        value != null ? Collections.singleton(value) : null,
        isBinary,
        valueTranscoder,
        modifications);
  }

  /**
   * Adds the specified values to the attribute with the specified name.
   *
   * @param <T> the type of the domain object
   * @param ldapEntry the ldap entry
   * @param name the attribute name
   * @param values the attribute values
   * @param isBinary specifies whether the attribute value is binary or not
   * @param valueTranscoder the value transcoder
   * @param modifications the list of modifications
   */
  static <T> void addAttributes(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final Collection<T> values,
      final boolean isBinary,
      @NotNull final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {
    final Collection<T> realValues = values == null ? null : values.stream()
        .filter(value -> {
          if (value instanceof CharSequence) {
            return ((CharSequence) value).length() > 0;
          }
          return value != null;
        })
        .collect(Collectors.toList());
    if (realValues == null || realValues.isEmpty()) {
      return;
    }
    final LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null) {
      final LdapAttribute newAttr = new LdapAttribute();
      newAttr.setBinary(isBinary);
      newAttr.setName(name);
      newAttr.addValues(valueTranscoder.encoder(), realValues);
      ldapEntry.addAttributes(newAttr);
      modifications.add(
          new AttributeModification(
              Type.ADD,
              newAttr));
    } else {
      final List<T> newValues = new ArrayList<>(
          getAttributeValues(ldapEntry, name, valueTranscoder));
      newValues.addAll(realValues);
      setAttributes(ldapEntry, name, newValues, attr.isBinary(), valueTranscoder, modifications);
    }
  }

  /**
   * Removes an attribute with the specified name.
   *
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param modifications the modifications
   */
  static void removeAttribute(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @NotNull final List<AttributeModification> modifications) {
    final LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null) {
      return;
    }
    ldapEntry.removeAttributes(attr);
    modifications.add(
        new AttributeModification(
            Type.DELETE,
            attr));
  }

  /**
   * Removes an attribute with the specified value. If the value is {@code null}, the whole attribute will be removed.
   *
   * @param <T> the type of the domain object
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param value the value
   * @param valueTranscoder the value transcoder
   * @param modifications the modifications
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
    if (value == null) {
      removeAttribute(ldapEntry, name, modifications);
    } else {
      removeAttributes(ldapEntry, name, Collections.singleton(value), valueTranscoder,
          modifications);
    }
  }

  /**
   * Remove attributes with the specified values. If values are empty or {@code null}, no attributes will be removed.
   *
   * @param <T> the type of the domain object
   * @param ldapEntry the ldap entry
   * @param name the name
   * @param values the values
   * @param valueTranscoder the value transcoder
   * @param modifications the modifications
   */
  static <T> void removeAttributes(
      @NotNull final LdapEntry ldapEntry,
      @NotNull final String name,
      @Nullable final Collection<T> values,
      final ValueTranscoder<T> valueTranscoder,
      @NotNull final List<AttributeModification> modifications) {

    final LdapAttribute attr = ldapEntry.getAttribute(name);
    if (attr == null || values == null || values.isEmpty()) {
      return;
    }
    final List<T> newValues = new ArrayList<>(getAttributeValues(ldapEntry, name, valueTranscoder));
    newValues.removeAll(values);
    setAttributes(ldapEntry, name, newValues, attr.isBinary(), valueTranscoder, modifications);
  }

  /**
   * Create dn string.
   *
   * @param rdn the rdn
   * @param rdnValue the rdn value
   * @param baseDn the base dn
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
