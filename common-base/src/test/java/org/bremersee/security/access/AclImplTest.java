package org.bremersee.security.access;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * The acl impl test.
 */
class AclImplTest {

  /**
   * Gets owner.
   */
  @Test
  void getOwner() {
    AclImpl model = new AclImpl("someone", null);
    assertEquals("someone", model.getOwner());

    model.setOwner("value");
    assertEquals("value", model.getOwner());

    AclImpl actual = new AclImpl("value", Collections.emptyMap());
    assertNotEquals(model, null);
    assertNotEquals(model, new Object());
    assertEquals(model, model);
    assertEquals(model, actual);
    assertEquals(model.hashCode(), actual.hashCode());

    assertTrue(model.toString().contains("value"));
  }

  /**
   * Entry map.
   */
  @Test
  void entryMap() {
    Ace write = new AceImpl();
    write.setGuest(false);
    write.getRoles().add("ROLE_ADMIN");
    Ace read = new AceImpl();
    read.setGuest(true);
    Map<String, Ace> value = new TreeMap<>();
    value.put("write", write);
    value.put("read", read);
    AclImpl model = new AclImpl("value", value);
    AclImpl actual = new AclImpl("value", model.entryMap());
    assertEquals(model, actual);
    assertEquals(model.hashCode(), actual.hashCode());
    assertTrue(model.toString().contains("ROLE_ADMIN"));
  }
}