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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.app.Group;
import org.bremersee.data.ldaptive.app.GroupMapper;
import org.bremersee.data.ldaptive.app.TestConfiguration;
import org.bremersee.data.ldaptive.app.Person;
import org.bremersee.data.ldaptive.app.PersonMapper;
import org.bremersee.exception.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.AddRequest;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The ldaptive template test.
 *
 * @author Christian Bremer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {TestConfiguration.class},
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "security.basic.enabled=false",
        "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
        "spring.ldap.embedded.credential.username=uid=admin",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=classpath:schema.ldif",
        "spring.ldap.embedded.port=12389",
        "spring.ldap.embedded.validation.enabled=false"
    })
@Slf4j
public class LdaptiveTemplateTest {

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  @Autowired
  private LdaptiveTemplate ldaptiveTemplate;

  @Autowired
  private PersonMapper personMapper;

  @Autowired
  private GroupMapper groupMapper;

  /**
   * Find existing persons.
   */
  @Test
  public void findExistingPersons() {
    SearchFilter searchFilter = new SearchFilter("(objectclass=inetOrgPerson)");
    SearchRequest searchRequest = new SearchRequest(
        "ou=people," + baseDn, searchFilter);
    searchRequest.setSearchScope(SearchScope.ONELEVEL);

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
  public void findExistingGroups() {
    SearchFilter searchFilter = new SearchFilter("(objectclass=groupOfUniqueNames)");
    SearchRequest searchRequest = new SearchRequest(
        "ou=groups," + baseDn, searchFilter);
    searchRequest.setSearchScope(SearchScope.ONELEVEL);

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
  public void findExistingPerson() {
    SearchFilter searchFilter = new SearchFilter("(&(objectclass=inetOrgPerson)(uid={0}))");
    searchFilter.setParameter(0, "anna");
    SearchRequest searchRequest = new SearchRequest(
        "ou=people," + baseDn, searchFilter);
    searchRequest.setSearchScope(SearchScope.ONELEVEL);

    Optional<LdapEntry> entry = ldaptiveTemplate.findOne(searchRequest);
    assertTrue(entry.isPresent());
    assertEquals("anna", entry.get().getAttribute("uid").getStringValue());

    Optional<Person> person = ldaptiveTemplate.findOne(searchRequest, personMapper);
    assertTrue(person.isPresent());
    assertEquals("anna", person.get().getUid());
  }

  /**
   * Exists group.
   */
  @Test
  public void existsGroup() {
    Group group = new Group();
    group.setCn("developers");
    assertTrue(ldaptiveTemplate.exists(group, groupMapper));
    group.setCn("na");
    assertFalse(ldaptiveTemplate.exists(group, groupMapper));
  }

  /**
   * Add and modify and delete person.
   */
  @Test
  public void addAndModifyAndDeletePerson() {
    Person person = new Person();
    person.setCn("A person");
    person.setSn("Person");
    person.setUid("person");

    String dn = personMapper.mapDn(person);
    LdapEntry destination = new LdapEntry();
    personMapper.map(person, destination);
    destination.setDn(dn);
    destination.addAttribute(new LdapAttribute(
        "objectclass",
        personMapper.getObjectClasses()));
    ldaptiveTemplate.add(new AddRequest(dn, destination.getAttributes()));
    assertTrue(ldaptiveTemplate.exists(person, personMapper));

    person.setSn("Surname");
    ModifyRequest modifyRequest = personMapper
        .mapAndComputeModifyRequest(person, destination);
    ldaptiveTemplate.modify(modifyRequest);

    person = ldaptiveTemplate.findOne(SearchRequest.newObjectScopeSearchRequest(dn), personMapper)
        .orElseThrow(() -> ServiceException.notFound("Person", "person"));
    assertEquals("Surname", person.getSn());

    person.setSn("");
    modifyRequest = personMapper
        .mapAndComputeModifyRequest(person, destination);
    ldaptiveTemplate.modify(modifyRequest);

    person = ldaptiveTemplate.findOne(SearchRequest.newObjectScopeSearchRequest(dn), personMapper)
        .orElseThrow(() -> ServiceException.notFound("Person", "person"));
    assertNull(person.getSn());

    ldaptiveTemplate.delete(new DeleteRequest(dn));
    assertFalse(ldaptiveTemplate.exists(person, personMapper));
  }

  /**
   * Save and delete group.
   */
  @Test
  public void saveAndDeleteGroup() {
    Group group = new Group();
    group.setCn("party");
    group.setOu("Party Guests");

    Set<String> members = new LinkedHashSet<>();
    members.add("uid=anna,ou=people," + baseDn);
    group.setMembers(members);

    group = ldaptiveTemplate.save(group, groupMapper);
    assertNotNull(group);

    SearchFilter searchFilter = new SearchFilter("(&(objectclass=groupOfUniqueNames)(cn={0}))");
    searchFilter.setParameter(0, "party");
    SearchRequest searchRequest = new SearchRequest(
        "ou=groups," + baseDn, searchFilter);
    searchRequest.setSearchScope(SearchScope.ONELEVEL);

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

    ldaptiveTemplate.delete(group, groupMapper);
    assertFalse(ldaptiveTemplate.exists(group, groupMapper));
  }

  /**
   * Save and delete persons.
   */
  @Test
  public void saveAndDeletePersons() {
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

    ldaptiveTemplate.deleteAll(Arrays.asList(p0, p1, p2), personMapper);
    assertFalse(ldaptiveTemplate.exists(p0, personMapper));
    assertFalse(ldaptiveTemplate.exists(p1, personMapper));
    assertFalse(ldaptiveTemplate.exists(p2, personMapper));
  }

}
