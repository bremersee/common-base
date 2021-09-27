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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.app.Group;
import org.bremersee.data.ldaptive.app.GroupMapper;
import org.bremersee.data.ldaptive.app.Person;
import org.bremersee.data.ldaptive.app.PersonMapper;
import org.bremersee.data.ldaptive.app.TestConfiguration;
import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ldaptive.AddRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.DeleteRequest;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.SimpleBindRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.util.SocketUtils;

/**
 * The ldaptive template test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = {TestConfiguration.class},
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "security.basic.enabled=false",
        "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
        "spring.ldap.embedded.credential.username=uid=admin",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=classpath:schema.ldif",
        "spring.ldap.embedded.validation.enabled=false"
    })
@Slf4j
class LdaptiveTemplateTest {

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  @Autowired
  private LdaptiveTemplate ldaptiveTemplate;

  @Autowired
  private PersonMapper personMapper;

  @Autowired
  private GroupMapper groupMapper;

  /**
   * Sets embedded ldap port.
   */
  @BeforeAll
  static void setEmbeddedLdapPort() {
    int embeddedLdapPort = SocketUtils.findAvailableTcpPort(10000);
    System.setProperty("spring.ldap.embedded.port", String.valueOf(embeddedLdapPort));
  }

  /**
   * Find existing persons.
   */
  @Test
  void findExistingPersons() {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn("ou=people," + baseDn)
        .filter("(objectclass=inetOrgPerson)")
        .scope(SearchScope.ONELEVEL)
        .build();

    // without mapper
    Collection<LdapEntry> entries = ldaptiveTemplate.findAll(searchRequest);
    entries.forEach(ldapEntry -> log.info("Ldap entry found with cn = {}",
        ldapEntry.getAttribute("cn").getStringValue()));
    assertTrue(entries.stream()
        .anyMatch(entry -> "Anna Livia Plurabelle"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));
    assertTrue(entries.stream()
        .anyMatch(entry -> "Gustav Anias Horn"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));
    assertTrue(entries.stream()
        .anyMatch(entry -> "Hans Castorp"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));

    // with mapper
    assertTrue(ldaptiveTemplate.findAll(searchRequest, personMapper)
        .anyMatch(entry -> "Anna Livia Plurabelle"
            .equalsIgnoreCase(entry.getCn())));
    assertTrue(ldaptiveTemplate.findAll(searchRequest, personMapper)
        .anyMatch(entry -> "Gustav Anias Horn"
            .equalsIgnoreCase(entry.getCn())));
    assertTrue(ldaptiveTemplate.findAll(searchRequest, personMapper)
        .anyMatch(entry -> "Hans Castorp"
            .equalsIgnoreCase(entry.getCn())));
  }

  /**
   * Find existing groups.
   */
  @Test
  void findExistingGroups() {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn("ou=groups," + baseDn)
        .filter("(objectclass=groupOfUniqueNames)")
        .scope(SearchScope.ONELEVEL)
        .build();

    // without mapper
    Collection<LdapEntry> entries = ldaptiveTemplate.findAll(searchRequest);
    entries.forEach(ldapEntry -> log.info("Ldap entry found with cn = {}",
        ldapEntry.getAttribute("cn").getStringValue()));
    assertTrue(entries.stream()
        .anyMatch(entry -> "developers"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));
    assertTrue(entries.stream()
        .anyMatch(entry -> "managers"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));

    // with mapper
    assertTrue(ldaptiveTemplate.findAll(searchRequest, groupMapper)
        .anyMatch(entry -> "developers"
            .equalsIgnoreCase(entry.getCn())));
    assertTrue(ldaptiveTemplate.findAll(searchRequest, groupMapper)
        .anyMatch(entry -> "managers"
            .equalsIgnoreCase(entry.getCn())));
  }

  /**
   * Find existing person.
   */
  @Test
  void findExistingPerson() {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn("ou=people," + baseDn)
        .filter(FilterTemplate.builder()
            .filter("(&(objectclass=inetOrgPerson)(uid={0}))")
            .parameters("anna")
            .build())
        .scope(SearchScope.ONELEVEL)
        .build();

    Optional<LdapEntry> entry = ldaptiveTemplate.findOne(searchRequest);
    assertTrue(entry.isPresent());
    assertEquals("anna", entry.get().getAttribute("uid").getStringValue());

    Optional<Person> person = ldaptiveTemplate.findOne(searchRequest, personMapper);
    assertTrue(person.isPresent());
    assertEquals("anna", person.get().getUid());
  }

  /**
   * Find existing person by dn.
   */
  @Test
  void findExistingPersonByDn() {
    String dn = "uid=anna,ou=people," + baseDn;
    Optional<LdapEntry> entry = ldaptiveTemplate
        .findOne(SearchRequest.objectScopeSearchRequest(dn));
    assertTrue(entry.isPresent());
  }

  /**
   * Find non existing person.
   */
  @Test
  void findNonExistingPerson() {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn("ou=people," + baseDn)
        .filter(FilterTemplate.builder()
            .filter("(&(objectclass=inetOrgPerson)(uid={0}))")
            .parameters(UUID.randomUUID().toString().replace("-", ""))
            .build())
        .scope(SearchScope.ONELEVEL)
        .build();

    Optional<LdapEntry> entry = ldaptiveTemplate.findOne(searchRequest);
    assertFalse(entry.isPresent());
  }

  /**
   * Find non existing person by dn.
   */
  @Test
  void findNonExistingPersonByDn() {
    String uid = UUID.randomUUID().toString().replace("-", "");
    String dn = "uid=" + uid + ",ou=people," + baseDn;
    Optional<LdapEntry> entry = ldaptiveTemplate
        .findOne(SearchRequest.objectScopeSearchRequest(dn));
    assertFalse(entry.isPresent());
  }

  /**
   * Generate user password and bind.
   */
  @Test
  void generateUserPasswordAndBind() {
    String dn = "uid=anna,ou=people," + baseDn;

    String initPasswd = ldaptiveTemplate.generateUserPassword(dn);
    assertNotNull(initPasswd);

    String newPasswd = initPasswd + "Pass1234";
    ldaptiveTemplate.modifyUserPassword(dn, initPasswd, newPasswd);

    boolean success = ldaptiveTemplate.bind(SimpleBindRequest.builder()
        .dn(dn)
        .password(newPasswd)
        .build());
    assertTrue(success);

    success = ldaptiveTemplate.bind(SimpleBindRequest.builder()
        .dn(dn)
        .password(initPasswd + "f")
        .build());
    assertFalse(success);
  }

  /**
   * Set user password fails.
   */
  @Test
  void setUserPasswordFails() {
    String dn = "uid=hans,ou=people," + baseDn;

    String initPasswd = ldaptiveTemplate.generateUserPassword(dn);
    assertNotNull(initPasswd);

    String newPasswd = initPasswd + "Pass1234";
    assertThrows(ServiceException.class, () -> ldaptiveTemplate.modifyUserPassword(dn, "wrong", newPasswd));
  }

  /**
   * Exists group.
   */
  @Test
  void existsGroup() {
    Group group = new Group();
    group.setCn("developers");
    assertTrue(ldaptiveTemplate.exists(group, groupMapper));
    group.setCn("na");
    assertFalse(ldaptiveTemplate.exists(group, groupMapper));
  }

  /**
   * Compare group.
   */
  @Test
  void compareGroup() {
    Group group = new Group();
    group.setCn("developers");

    assertTrue(ldaptiveTemplate.compare(CompareRequest.builder()
        .dn(groupMapper.mapDn(group))
        .name("ou")
        .value("developer")
        .build()));

    assertFalse(ldaptiveTemplate.compare(CompareRequest.builder()
        .dn(groupMapper.mapDn(group))
        .name("ou")
        .value("manager")
        .build()));

    assertFalse(ldaptiveTemplate.compare(CompareRequest.builder()
        .dn(groupMapper.mapDn(group))
        .name("notexists")
        .value("manager")
        .build()));

    group.setCn("notexists");
    assertThrows(ServiceException.class, () -> ldaptiveTemplate.compare(CompareRequest.builder()
        .dn(groupMapper.mapDn(group))
        .name("ou")
        .value("developer")
        .build()));
  }

  /**
   * Add and modify and delete person.
   */
  @Test
  void addAndModifyAndDeletePerson() {
    Person person = new Person();
    person.setCn("A person");
    person.setSn("Person");
    person.setUid("person");

    String dn = personMapper.mapDn(person);
    LdapEntry destination = new LdapEntry();
    personMapper.map(person, destination);
    destination.setDn(dn);
    destination.addAttributes(new LdapAttribute(
        "objectclass",
        personMapper.getObjectClasses()));
    ldaptiveTemplate.add(new AddRequest(dn, destination.getAttributes()));
    assertTrue(ldaptiveTemplate.exists(person, personMapper));

    person.setSn("Surname");
    ModifyRequest modifyRequest = personMapper
        .mapAndComputeModifyRequest(person, destination);
    ldaptiveTemplate.modify(modifyRequest);

    person = ldaptiveTemplate.findOne(SearchRequest.objectScopeSearchRequest(dn), personMapper)
        .orElseThrow(() -> ServiceException.notFound("Person", "person"));
    assertEquals("Surname", person.getSn());

    person.setSn("");
    modifyRequest = personMapper
        .mapAndComputeModifyRequest(person, destination);
    ldaptiveTemplate.modify(modifyRequest);

    person = ldaptiveTemplate.findOne(SearchRequest.objectScopeSearchRequest(dn), personMapper)
        .orElseThrow(() -> ServiceException.notFound("Person", "person"));
    assertNull(person.getSn());

    ldaptiveTemplate.delete(new DeleteRequest(dn));
    assertFalse(ldaptiveTemplate.exists(person, personMapper));
  }

  /**
   * Save and remove group.
   */
  @Test
  void saveAndRemoveGroup() {
    Group group = new Group();
    group.setCn("party");
    group.setOu("Party Guests");

    Set<String> members = new LinkedHashSet<>();
    members.add("uid=anna,ou=people," + baseDn);
    group.setMembers(members);

    group = ldaptiveTemplate.save(group, groupMapper);
    assertNotNull(group);

    SearchRequest searchRequest = SearchRequest.builder()
        .dn("ou=groups," + baseDn)
        .filter(FilterTemplate.builder()
            .filter("(&(objectclass=groupOfUniqueNames)(cn={0}))")
            .parameters("party")
            .build())
        .scope(SearchScope.ONELEVEL)
        .build();

    group = ldaptiveTemplate.findOne(searchRequest, groupMapper)
        .orElseThrow(() -> ServiceException.notFound("Group", "party"));
    assertEquals("party", group.getCn());
    assertEquals("Party Guests", group.getOu());
    assertTrue(group.getMembers().contains("uid=anna,ou=people," + baseDn));

    group.getMembers().add("uid=hans,ou=people," + baseDn);
    group.getMembers().add("uid=gustav,ou=people," + baseDn);
    group = ldaptiveTemplate.save(group, groupMapper);
    assertNotNull(group);

    group = ldaptiveTemplate.findOne(searchRequest, groupMapper)
        .orElseThrow(() -> ServiceException.notFound("Group", "party"));
    assertEquals("party", group.getCn());
    assertEquals("Party Guests", group.getOu());
    assertTrue(group.getMembers().contains("uid=anna,ou=people," + baseDn));
    assertTrue(group.getMembers().contains("uid=hans,ou=people," + baseDn));
    assertTrue(group.getMembers().contains("uid=gustav,ou=people," + baseDn));

    group.getMembers().remove("uid=hans,ou=people," + baseDn);
    group = ldaptiveTemplate.save(group, groupMapper);
    assertNotNull(group);

    group = ldaptiveTemplate.findOne(searchRequest, groupMapper)
        .orElseThrow(() -> ServiceException.notFound("Group", "party"));
    assertEquals("party", group.getCn());
    assertEquals("Party Guests", group.getOu());
    assertTrue(group.getMembers().contains("uid=anna,ou=people," + baseDn));
    assertTrue(group.getMembers().contains("uid=gustav,ou=people," + baseDn));
    assertFalse(group.getMembers().contains("uid=hans,ou=people," + baseDn));

    ldaptiveTemplate.remove(group, groupMapper);
    assertFalse(ldaptiveTemplate.exists(group, groupMapper));
  }

  /**
   * Save and delete persons.
   */
  @Test
  void saveAndRemovePersons() {
    Person p0 = new Person();
    p0.setCn("Person Number 0");
    p0.setSn("Person 0");
    p0.setUid("person0");
    Person p1 = new Person();
    p1.setCn("Person Number 1");
    p1.setSn("Person 1");
    p1.setUid("person1");
    Person p2 = new Person();
    p2.setCn("Person Number 2");
    p2.setSn("Person 2");
    p2.setUid("person2");

    ldaptiveTemplate.saveAll(Arrays.asList(p0, p1, p2), personMapper)
        .forEach(person -> log.info("New person: {}", person));
    assertTrue(ldaptiveTemplate.exists(p0, personMapper));
    assertTrue(ldaptiveTemplate.exists(p1, personMapper));
    assertTrue(ldaptiveTemplate.exists(p2, personMapper));

    ldaptiveTemplate.removeAll(Arrays.asList(p0, p1, p2), personMapper);
    assertFalse(ldaptiveTemplate.exists(p0, personMapper));
    assertFalse(ldaptiveTemplate.exists(p1, personMapper));
    assertFalse(ldaptiveTemplate.exists(p2, personMapper));
  }

  /**
   * Modify dn.
   */
  @Test
  void modifyDn() {
    Person p0 = new Person();
    p0.setCn("Gottfried");
    p0.setSn("Benn");
    p0.setUid("benn");

    p0 = ldaptiveTemplate.save(p0, personMapper);
    assertNotNull(p0);

    ldaptiveTemplate.modifyDn(ModifyDnRequest.builder()
        .oldDN(personMapper.mapDn(p0))
        .newRDN("uid=gbn")
        .delete(true)
        .build());

    Optional<Person> pr = ldaptiveTemplate.findAll(
            SearchRequest.builder()
                .dn("ou=people," + baseDn)
                .filter("(sn=Benn)")
                .build(),
            personMapper)
        .filter(p -> "gbn".equals(p.getUid()))
        .findAny();
    assertTrue(pr.isPresent());

    ldaptiveTemplate.delete(DeleteRequest.builder().dn(personMapper.mapDn(pr.get())).build());
  }

  /**
   * Test clone.
   */
  @Test
  void testClone() {
    LdaptiveTemplate clone = ldaptiveTemplate.clone();
    clone.setErrorHandler(new DefaultLdaptiveErrorHandler());
    Group group = new Group();
    group.setCn("developers");
    assertTrue(clone.exists(group, groupMapper));
    group.setCn("na");
    assertFalse(clone.exists(group, groupMapper));
  }

}
