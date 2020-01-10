package org.bremersee.security.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * The ace impl test.
 */
class AceImplTest {

  /**
   * Is guest.
   */
  @Test
  void isGuest() {
    AceImpl model = new AceImpl();
    model.setGuest(true);
    assertTrue(model.isGuest());

    AceImpl actual = new AceImpl();
    actual.setGuest(false);
    assertFalse(actual.isGuest());

    actual.setGuest(true);

    assertNotEquals(model, null);
    assertNotEquals(model, new Object());
    assertEquals(model, model);
    assertEquals(model, actual);
    assertEquals(model.hashCode(), actual.hashCode());

    assertTrue(model.toString().contains("true"));
  }

  /**
   * Gets users.
   */
  @Test
  void getUsers() {
    Set<String> value = new TreeSet<>();
    value.add("value9");
    value.add("value3");
    AceImpl model = new AceImpl();
    model.getUsers().addAll(value);
    assertEquals(value, model.getUsers());

    AceImpl actual = new AceImpl();
    actual.getUsers().addAll(value);
    assertEquals(model, actual);
    assertEquals(model.hashCode(), actual.hashCode());

    assertTrue(model.toString().contains(value.toString()));
  }

  /**
   * Gets roles.
   */
  @Test
  void getRoles() {
    Set<String> value = new TreeSet<>();
    value.add("value9");
    value.add("value3");
    AceImpl model = new AceImpl();
    model.getRoles().addAll(value);
    assertEquals(value, model.getRoles());

    AceImpl actual = new AceImpl();
    actual.getRoles().addAll(value);
    assertEquals(model, actual);
    assertEquals(model.hashCode(), actual.hashCode());

    assertTrue(model.toString().contains(value.toString()));
  }

  /**
   * Gets groups.
   */
  @Test
  void getGroups() {
    Set<String> value = new TreeSet<>();
    value.add("value9");
    value.add("value3");
    AceImpl model = new AceImpl();
    model.getGroups().addAll(value);
    assertEquals(value, model.getGroups());

    AceImpl actual = new AceImpl();
    actual.getGroups().addAll(value);
    assertEquals(model, actual);
    assertEquals(model.hashCode(), actual.hashCode());

    assertTrue(model.toString().contains(value.toString()));
  }

  /**
   * Tree set.
   */
  @Test
  void treeSet() {
    TreeSet<String> expected = new TreeSet<>();
    assertEquals(expected, AceImpl.treeSet(null));
    assertEquals(expected, AceImpl.treeSet(new TreeSet<>()));
    expected.add("value9");
    expected.add("value3");
    assertEquals(expected, AceImpl.treeSet(Arrays.asList("value9", "value3")));
  }
}