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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyRequest;
import org.ldaptive.io.ByteArrayValueTranscoder;
import org.ldaptive.io.StringValueTranscoder;
import org.springframework.util.StringUtils;

/**
 * The ldaptive entry mapper test.
 *
 * @author Christian Bremer
 */
class LdaptiveEntryMapperTest {

  private static final StringValueTranscoder STRING_TRANSCODER = new StringValueTranscoder();

  private static final ByteArrayValueTranscoder BYTE_TRANSCODER = new ByteArrayValueTranscoder();

  private static final PersonMapper mapper = new PersonMapper();

  /**
   * Gets attribute value.
   */
  @Test
  void getAttributeValue() {
    assertNull(LdaptiveEntryMapper
        .getAttributeValue(null, "foo", null, null));
    String expected = "bar";
    assertEquals(expected, LdaptiveEntryMapper
        .getAttributeValue(null, "foo", null, "bar"));
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("foo", expected));
    String actual = LdaptiveEntryMapper
        .getAttributeValue(entry, "foo", STRING_TRANSCODER, null);
    assertEquals(expected, actual);
    actual = LdaptiveEntryMapper
        .getAttributeValue(entry, "na", STRING_TRANSCODER, "bar");
    assertEquals(expected, actual);
    assertNull(LdaptiveEntryMapper
        .getAttributeValue(entry, "na", STRING_TRANSCODER, null));
  }

  /**
   * Gets attribute values.
   */
  @Test
  void getAttributeValues() {
    assertTrue(LdaptiveEntryMapper
        .getAttributeValues(null, "foo", STRING_TRANSCODER).isEmpty());
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("key", "foo", "bar"));
    assertTrue(LdaptiveEntryMapper
        .getAttributeValues(entry, "foo", STRING_TRANSCODER).isEmpty());
    Collection<String> actual = LdaptiveEntryMapper
        .getAttributeValues(entry, "key", STRING_TRANSCODER);
    assertTrue(actual.contains("foo"));
    assertTrue(actual.contains("bar"));
  }

  /**
   * Gets attribute values as set.
   */
  @Test
  void getAttributeValuesAsSet() {
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("key", "foo", "bar"));
    Set<String> actual = LdaptiveEntryMapper
        .getAttributeValuesAsSet(entry, "key", STRING_TRANSCODER);
    assertTrue(actual.contains("foo"));
    assertTrue(actual.contains("bar"));
  }

  /**
   * Gets attribute values as list.
   */
  @Test
  void getAttributeValuesAsList() {
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("key", "foo", "bar"));
    List<String> actual = LdaptiveEntryMapper
        .getAttributeValuesAsList(entry, "key", STRING_TRANSCODER);
    assertTrue(actual.contains("foo"));
    assertTrue(actual.contains("bar"));
  }

  /**
   * Sets attribute.
   */
  @Test
  void setAttribute() {
    List<AttributeModification> modifications = new ArrayList<>();
    LdapEntry entry = new LdapEntry();

    // set value
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", "bar", false, STRING_TRANSCODER, modifications);
    assertEquals("bar", entry.getAttribute("foo").getStringValue());
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.ADD,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // change value
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", "no-bar", false, STRING_TRANSCODER, modifications);
    assertEquals("no-bar", entry.getAttribute("foo").getStringValue());
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // once again with the same value
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", "no-bar", false, STRING_TRANSCODER, modifications);
    assertEquals("no-bar", entry.getAttribute("foo").getStringValue());
    assertEquals(0, modifications.size()); // no modifications!

    // delete value = remove attribute
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", null, false, STRING_TRANSCODER, modifications);
    assertNull(entry.getAttribute("foo"));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REMOVE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // set bytes
    byte[] bytes = "bar".getBytes(StandardCharsets.UTF_8);
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", bytes, true, BYTE_TRANSCODER, modifications);
    assertArrayEquals(bytes, entry.getAttribute("foo").getBinaryValue());
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.ADD,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // change bytes
    bytes = "no-bar".getBytes(StandardCharsets.UTF_8);
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", bytes, true, BYTE_TRANSCODER, modifications);
    assertArrayEquals(bytes, entry.getAttribute("foo").getBinaryValue());
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // once again with the same value
    LdaptiveEntryMapper.setAttribute(
        entry, "foo", bytes, true, BYTE_TRANSCODER, modifications);
    assertArrayEquals(bytes, entry.getAttribute("foo").getBinaryValue());
    assertEquals(0, modifications.size()); // no modifications!
  }

  /**
   * Sets attributes.
   */
  @Test
  void setAttributes() {
    List<AttributeModification> modifications = new ArrayList<>();
    List<String> expected = Arrays.asList("anna", "livia");
    LdapEntry entry = new LdapEntry();

    // set values
    LdaptiveEntryMapper.setAttributes(
        entry, "foo", expected, false, STRING_TRANSCODER, modifications);
    assertTrue(entry.getAttribute("foo").getStringValues().containsAll(expected));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.ADD,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // change values
    expected = Arrays.asList("hans", "castorp");
    LdaptiveEntryMapper.setAttributes(
        entry, "foo", expected, false, STRING_TRANSCODER, modifications);
    assertTrue(entry.getAttribute("foo").getStringValues().containsAll(expected));
    assertFalse(entry.getAttribute("foo").getStringValues().contains("anna"));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();
  }

  /**
   * Add attribute.
   */
  @Test
  void addAttribute() {
    List<AttributeModification> modifications = new ArrayList<>();
    LdapEntry entry = new LdapEntry();

    // add value
    LdaptiveEntryMapper.addAttribute(
        entry, "foo", "anna", false, STRING_TRANSCODER, modifications);
    assertEquals("anna", entry.getAttribute("foo").getStringValue());
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.ADD,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // add another value
    LdaptiveEntryMapper.addAttribute(
        entry, "foo", "livia", false, STRING_TRANSCODER, modifications);
    assertTrue(entry.getAttribute("foo").getStringValues()
        .containsAll(Arrays.asList("anna", "livia")));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    // add null
    LdaptiveEntryMapper.addAttribute(
        entry, "foo", null, false, STRING_TRANSCODER, modifications);
    assertTrue(entry.getAttribute("foo").getStringValues()
        .containsAll(Arrays.asList("anna", "livia")));
    assertEquals(0, modifications.size());
    modifications.clear();
  }

  /**
   * Add attributes.
   */
  @Test
  void addAttributes() {
    List<AttributeModification> modifications = new ArrayList<>();
    LdapEntry entry = new LdapEntry();
    byte[] annaBytes = "anna".getBytes(StandardCharsets.UTF_8);
    byte[] liviaBytes = "livia".getBytes(StandardCharsets.UTF_8);
    List<byte[]> bytes = new ArrayList<>();
    bytes.add(annaBytes);
    bytes.add(liviaBytes);
    LdaptiveEntryMapper.addAttributes(
        entry, "foo", bytes, true, BYTE_TRANSCODER, modifications);
    assertTrue(entry.getAttribute("foo").getBinaryValues().contains(annaBytes));
    assertTrue(entry.getAttribute("foo").getBinaryValues().contains(liviaBytes));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.ADD,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    byte[] hansBytes = "hans".getBytes(StandardCharsets.UTF_8);
    byte[] castorpBytes = "castorp".getBytes(StandardCharsets.UTF_8);
    bytes = new ArrayList<>();
    bytes.add(hansBytes);
    bytes.add(castorpBytes);
    LdaptiveEntryMapper.addAttributes(
        entry, "foo", bytes, true, BYTE_TRANSCODER, modifications);
    assertTrue(entry.getAttribute("foo").getBinaryValues().contains(annaBytes));
    assertTrue(entry.getAttribute("foo").getBinaryValues().contains(liviaBytes));
    assertTrue(entry.getAttribute("foo").getBinaryValues().contains(hansBytes));
    assertTrue(entry.getAttribute("foo").getBinaryValues().contains(castorpBytes));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();
  }

  /**
   * Remove attribute.
   */
  @Test
  void removeAttribute() {
    List<AttributeModification> modifications = new ArrayList<>();
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("foo", "anna", "livia"));

    LdaptiveEntryMapper.removeAttribute(entry, "foo", modifications);
    assertNull(entry.getAttribute("foo"));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REMOVE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    entry.addAttribute(new LdapAttribute("foo", "anna", "livia"));
    LdaptiveEntryMapper
        .removeAttribute(entry, "foo", "anna", STRING_TRANSCODER, modifications);
    assertFalse(entry.getAttribute("foo").getStringValues().contains("anna"));
    assertTrue(entry.getAttribute("foo").getStringValues().contains("livia"));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();

    LdaptiveEntryMapper
        .removeAttribute(entry, "foo", "livia", STRING_TRANSCODER, modifications);
    assertNull(entry.getAttribute("foo"));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REMOVE,
        modifications.get(0).getAttributeModificationType());
    modifications.clear();
  }

  /**
   * Remove attributes.
   */
  @Test
  void removeAttributes() {
    List<AttributeModification> modifications = new ArrayList<>();
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("foo", "anna", "livia", "hans", "castorp"));

    LdaptiveEntryMapper
        .removeAttributes(entry, "foo", Arrays.asList("livia", "castorp"), STRING_TRANSCODER,
            modifications);
    assertFalse(entry.getAttribute("foo").getStringValues().contains("livia"));
    assertFalse(entry.getAttribute("foo").getStringValues().contains("castorp"));
    assertTrue(entry.getAttribute("foo").getStringValues().contains("anna"));
    assertTrue(entry.getAttribute("foo").getStringValues().contains("hans"));
    assertEquals(1, modifications.size());
    assertEquals(
        AttributeModificationType.REPLACE,
        modifications.get(0).getAttributeModificationType());
  }

  /**
   * Create dn.
   */
  @Test
  void createDn() {
    assertEquals(
        "cn=anna,cn=users,dc=example,dc=org",
        LdaptiveEntryMapper.createDn("cn", "anna", "cn=users,dc=example,dc=org"));
  }

  /**
   * Gets rdn.
   */
  @Test
  void getRdn() {
    assertNull(LdaptiveEntryMapper.getRdn(null));
    assertEquals("no-real-dn", LdaptiveEntryMapper.getRdn("no-real-dn"));
    assertEquals("anna", LdaptiveEntryMapper.getRdn("cn=anna,cn=users,dc=example,dc=org"));
  }

  /**
   * Map dn.
   */
  @Test
  void mapDn() {
    assertEquals(
        "cn=anna,dc=example,dc=org",
        mapper.mapDn(Person.builder().name("anna").build()));
  }

  /**
   * Map.
   */
  @Test
  void map() {
    LdapEntry entry = new LdapEntry();
    entry.addAttribute(new LdapAttribute("name", "hans"));
    entry.addAttribute(new LdapAttribute(
        "photo",
        "xyz".getBytes(StandardCharsets.UTF_8)));
    entry.addAttribute(new LdapAttribute(
        "mail",
        "hans@example.org", "castorp@example.org"));
    Person person = mapper.map(entry);
    assertNotNull(person);
    assertEquals("hans", person.getName());
    assertArrayEquals("xyz".getBytes(StandardCharsets.UTF_8), person.getPhoto());
    assertTrue(person.getMailAddresses().contains("hans@example.org"));
    assertTrue(person.getMailAddresses().contains("castorp@example.org"));
  }

  /**
   * Map and compute modify request.
   */
  @Test
  void mapAndComputeModifyRequest() {
    Person person = Person.builder()
        .name("hans")
        .photo("zzz".getBytes(StandardCharsets.UTF_8))
        .mailAddresses(Arrays.asList("hans@example.org", "castorp@example.org"))
        .build();
    LdapEntry entry = new LdapEntry();
    entry.setDn("cn=hans,dc=example,dc=org");
    entry.addAttribute(new LdapAttribute("name", "hans"));
    entry.addAttribute(new LdapAttribute(
        "photo",
        "xyz".getBytes(StandardCharsets.UTF_8)));
    entry.addAttribute(new LdapAttribute(
        "mail",
        "hans.castorp@example.org"));
    ModifyRequest request = mapper.mapAndComputeModifyRequest(person, entry);
    assertNotNull(request);
    assertEquals(2, request.getAttributeModifications().length);
    assertEquals("hans", entry.getAttribute("name").getStringValue());
    assertArrayEquals(
        "zzz".getBytes(StandardCharsets.UTF_8),
        entry.getAttribute("photo").getBinaryValue());
    assertTrue(entry.getAttribute("mail").getStringValues().contains("hans@example.org"));
    assertTrue(entry.getAttribute("mail").getStringValues().contains("castorp@example.org"));
    assertFalse(entry.getAttribute("mail").getStringValues()
        .contains("hans.castorp@example.org"));
  }

  /**
   * The test person.
   */
  @Data
  @Builder
  static class Person {

    private String name;

    private List<String> mailAddresses;

    private byte[] photo;

  }

  /**
   * The test mapper.
   */
  static class PersonMapper implements LdaptiveEntryMapper<Person> {

    @Override
    public String[] getObjectClasses() {
      return new String[0];
    }

    @Override
    public String mapDn(Person person) {
      if (person == null || !StringUtils.hasText(person.getName())) {
        return null;
      }
      return LdaptiveEntryMapper.createDn("cn", person.getName(), "dc=example,dc=org");
    }

    @Override
    public void map(LdapEntry ldapEntry, Person person) {
      person.setMailAddresses(LdaptiveEntryMapper
          .getAttributeValuesAsList(ldapEntry, "mail", STRING_TRANSCODER));
      person.setName(LdaptiveEntryMapper
          .getAttributeValue(ldapEntry, "name", STRING_TRANSCODER, null));
      person.setPhoto(LdaptiveEntryMapper
          .getAttributeValue(ldapEntry, "photo", BYTE_TRANSCODER, null));
    }

    @Override
    public Person map(LdapEntry ldapEntry) {
      if (ldapEntry == null) {
        return null;
      }
      Person person = Person.builder().build();
      map(ldapEntry, person);
      return person;
    }

    @Override
    public AttributeModification[] mapAndComputeModifications(
        @NotNull Person source,
        @NotNull LdapEntry destination) {
      List<AttributeModification> modifications = new ArrayList<>();
      LdaptiveEntryMapper.setAttribute(destination,
          "name", source.getName(), false, STRING_TRANSCODER, modifications);
      LdaptiveEntryMapper.setAttributes(destination,
          "mail", source.getMailAddresses(), false, STRING_TRANSCODER, modifications);
      LdaptiveEntryMapper.setAttribute(destination,
          "photo", source.getPhoto(), true, BYTE_TRANSCODER, modifications);
      return modifications.toArray(new AttributeModification[0]);
    }
  }
}
