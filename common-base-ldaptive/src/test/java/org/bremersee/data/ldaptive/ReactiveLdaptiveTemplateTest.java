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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.app.Group;
import org.bremersee.data.ldaptive.app.GroupMapper;
import org.bremersee.data.ldaptive.app.Person;
import org.bremersee.data.ldaptive.app.PersonMapper;
import org.bremersee.data.ldaptive.app.TestConfiguration;
import org.bremersee.data.ldaptive.reactive.ReactiveLdaptiveTemplate;
import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.AddRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionFactory;
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
import reactor.test.StepVerifier;

/**
 * The reactive ldaptive template test.
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
class ReactiveLdaptiveTemplateTest {

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private PersonMapper personMapper;

  @Autowired
  private GroupMapper groupMapper;

  private ReactiveLdaptiveTemplate ldaptiveTemplate;

  /**
   * Sets embedded ldap port.
   */
  @BeforeAll
  static void setEmbeddedLdapPort() {
    int embeddedLdapPort = SocketUtils.findAvailableTcpPort(10000);
    System.setProperty("spring.ldap.embedded.port", String.valueOf(embeddedLdapPort));
  }

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ldaptiveTemplate = new ReactiveLdaptiveTemplate(connectionFactory);
    ldaptiveTemplate.setErrorHandler(new DefaultLdaptiveErrorHandler());
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

    Set<String> names = Set.of("Anna Livia Plurabelle", "Gustav Anias Horn", "Hans Castorp");
    StepVerifier.create(ldaptiveTemplate.findAll(searchRequest))
        .assertNext(ldapEntry -> assertTrue(names.contains(ldapEntry.getAttribute("cn").getStringValue())))
        .assertNext(ldapEntry -> assertTrue(names.contains(ldapEntry.getAttribute("cn").getStringValue())))
        .assertNext(ldapEntry -> assertTrue(names.contains(ldapEntry.getAttribute("cn").getStringValue())))
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.findAll(searchRequest, personMapper))
        .assertNext(person -> assertTrue(names.contains(person.getCn())))
        .assertNext(person -> assertTrue(names.contains(person.getCn())))
        .assertNext(person -> assertTrue(names.contains(person.getCn())))
        .verifyComplete();
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

    Set<String> names = Set.of("developers", "managers");
    // without mapper
    StepVerifier.create(ldaptiveTemplate.findAll(searchRequest))
        .assertNext(ldapEntry -> assertTrue(names.contains(ldapEntry.getAttribute("cn").getStringValue())))
        .assertNext(ldapEntry -> assertTrue(names.contains(ldapEntry.getAttribute("cn").getStringValue())))
        .verifyComplete();

    // with mapper
    StepVerifier.create(ldaptiveTemplate.findAll(searchRequest, groupMapper))
        .assertNext(group -> assertTrue(names.contains(group.getCn())))
        .assertNext(group -> assertTrue(names.contains(group.getCn())))
        .verifyComplete();
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

    StepVerifier.create(ldaptiveTemplate.findOne(searchRequest))
        .assertNext(ldapEntry -> assertEquals("anna", ldapEntry.getAttribute("uid").getStringValue()))
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.findOne(searchRequest, personMapper))
        .assertNext(person -> assertEquals("anna", person.getUid()))
        .verifyComplete();
  }

  /**
   * Find existing person by dn.
   */
  @Test
  void findExistingPersonByDn() {
    String dn = "uid=anna,ou=people," + baseDn;
    StepVerifier.create(ldaptiveTemplate.findOne(SearchRequest.objectScopeSearchRequest(dn)))
        .assertNext(ldapEntry -> assertEquals("anna", ldapEntry.getAttribute("uid").getStringValue()))
        .verifyComplete();
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

    StepVerifier.create(ldaptiveTemplate.findOne(searchRequest))
        .verifyComplete();
  }

  /**
   * Find non existing person by dn.
   */
  @Test
  void findNonExistingPersonByDn() {
    String uid = UUID.randomUUID().toString().replace("-", "");
    String dn = "uid=" + uid + ",ou=people," + baseDn;
    StepVerifier.create(ldaptiveTemplate.findOne(SearchRequest.objectScopeSearchRequest(dn)))
        .verifyComplete();
  }

  /**
   * Generate user password and bind.
   */
  @Test
  void generateUserPasswordAndBind() {
    String dn = "uid=anna,ou=people," + baseDn;

    String newPasswd = "Pass1234$";
    StepVerifier.create(ldaptiveTemplate.generateUserPassword(dn))
        .assertNext(initPasswd -> StepVerifier
            .create(ldaptiveTemplate.modifyUserPassword(dn, initPasswd, newPasswd))
            .assertNext(extendedResponse -> {
              assertTrue(extendedResponse.isSuccess());
              StepVerifier.create(ldaptiveTemplate.bind(SimpleBindRequest.builder()
                      .dn(dn)
                      .password(newPasswd)
                      .build()))
                  .expectNext(true)
                  .verifyComplete();
            })
            .verifyComplete())
        .verifyComplete();
  }

  /**
   * Exists group.
   */
  @Test
  void existsGroup() {
    Group group = new Group();
    group.setCn("developers");
    StepVerifier.create(ldaptiveTemplate.exists(group, groupMapper))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();

    group.setCn("na");
    StepVerifier.create(ldaptiveTemplate.exists(group, groupMapper))
        .assertNext(Assertions::assertFalse)
        .verifyComplete();
  }

  /**
   * Compare group.
   */
  @Test
  void compareGroup() {
    Group group = new Group();
    group.setCn("developers");

    StepVerifier
        .create(ldaptiveTemplate.compare(CompareRequest.builder()
            .dn(groupMapper.mapDn(group))
            .name("ou")
            .value("developer")
            .build()))
        .expectNext(true)
        .verifyComplete();

    StepVerifier
        .create(ldaptiveTemplate.compare(CompareRequest.builder()
            .dn(groupMapper.mapDn(group))
            .name("ou")
            .value("manager")
            .build()))
        .expectNext(false)
        .verifyComplete();

    StepVerifier
        .create(ldaptiveTemplate.compare(CompareRequest.builder()
            .dn(groupMapper.mapDn(group))
            .name("notexists")
            .value("manager")
            .build()))
        .expectNext(false)
        .verifyComplete();

    Group notExists = new Group();
    notExists.setCn("notexists");
    StepVerifier
        .create(ldaptiveTemplate.compare(CompareRequest.builder()
            .dn(groupMapper.mapDn(notExists))
            .name("ou")
            .value("developer")
            .build()))
        .expectError(ServiceException.class)
        .verify();
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

    StepVerifier.create(ldaptiveTemplate.add(new AddRequest(dn, destination.getAttributes())))
        .assertNext(result -> assertTrue(result.isSuccess()))
        .verifyComplete();
    StepVerifier.create(ldaptiveTemplate.exists(person, personMapper))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();

    person.setSn("Surname");
    ModifyRequest modifyRequest = personMapper
        .mapAndComputeModifyRequest(person, destination);
    StepVerifier.create(ldaptiveTemplate.modify(modifyRequest))
        .assertNext(result -> assertTrue(result.isSuccess()))
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.findOne(SearchRequest.objectScopeSearchRequest(dn), personMapper))
        .assertNext(p -> assertEquals("Surname", p.getSn()))
        .verifyComplete();

    person.setSn("");
    modifyRequest = personMapper.mapAndComputeModifyRequest(person, destination);
    StepVerifier.create(ldaptiveTemplate.modify(modifyRequest))
        .assertNext(result -> assertTrue(result.isSuccess()))
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.findOne(SearchRequest.objectScopeSearchRequest(dn), personMapper))
        .assertNext(p -> assertNull(p.getSn()))
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.delete(new DeleteRequest(dn)))
        .assertNext(result -> assertTrue(result.isSuccess()))
        .verifyComplete();
    StepVerifier.create(ldaptiveTemplate.exists(person, personMapper))
        .assertNext(Assertions::assertFalse)
        .verifyComplete();
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

    StepVerifier.create(ldaptiveTemplate.save(group, groupMapper))
        .assertNext(g -> {
          assertEquals("party", g.getCn());
          assertEquals("Party Guests", g.getOu());
          assertTrue(g.getMembers().contains("uid=anna,ou=people," + baseDn));
        })
        .verifyComplete();

    SearchRequest searchRequest = SearchRequest.builder()
        .dn("ou=groups," + baseDn)
        .filter(FilterTemplate.builder()
            .filter("(&(objectclass=groupOfUniqueNames)(cn={0}))")
            .parameters("party")
            .build())
        .scope(SearchScope.ONELEVEL)
        .build();

    StepVerifier.create(ldaptiveTemplate.findOne(searchRequest, groupMapper))
        .assertNext(g -> {
          assertEquals("party", g.getCn());
          assertEquals("Party Guests", g.getOu());
          assertTrue(g.getMembers().contains("uid=anna,ou=people," + baseDn));
        })
        .verifyComplete();

    group.getMembers().add("uid=hans,ou=people," + baseDn);
    group.getMembers().add("uid=gustav,ou=people," + baseDn);
    StepVerifier.create(ldaptiveTemplate.save(group, groupMapper))
        .assertNext(g -> {
          assertTrue(g.getMembers().contains("uid=anna,ou=people," + baseDn));
          assertTrue(g.getMembers().contains("uid=hans,ou=people," + baseDn));
          assertTrue(g.getMembers().contains("uid=gustav,ou=people," + baseDn));
        })
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.findOne(searchRequest, groupMapper))
        .assertNext(g -> {
          assertTrue(g.getMembers().contains("uid=anna,ou=people," + baseDn));
          assertTrue(g.getMembers().contains("uid=hans,ou=people," + baseDn));
          assertTrue(g.getMembers().contains("uid=gustav,ou=people," + baseDn));
        })
        .verifyComplete();

    group.getMembers().remove("uid=hans,ou=people," + baseDn);
    StepVerifier.create(ldaptiveTemplate.save(group, groupMapper))
        .assertNext(g -> {
          assertTrue(g.getMembers().contains("uid=anna,ou=people," + baseDn));
          assertFalse(g.getMembers().contains("uid=hans,ou=people," + baseDn));
          assertTrue(g.getMembers().contains("uid=gustav,ou=people," + baseDn));
        })
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.findOne(searchRequest, groupMapper))
        .assertNext(g -> {
          assertTrue(g.getMembers().contains("uid=anna,ou=people," + baseDn));
          assertFalse(g.getMembers().contains("uid=hans,ou=people," + baseDn));
          assertTrue(g.getMembers().contains("uid=gustav,ou=people," + baseDn));
        })
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.remove(group, groupMapper))
        .assertNext(r -> assertTrue(r.isSuccess()))
        .verifyComplete();
    StepVerifier.create(ldaptiveTemplate.exists(group, groupMapper))
        .assertNext(Assertions::assertFalse)
        .verifyComplete();
  }

  /**
   * Save and delete persons.
   */
  @Test
  void saveAndDeletePersons() {
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

    StepVerifier.create(ldaptiveTemplate.saveAll(List.of(p0, p1, p2), personMapper))
        .expectNextCount(3L)
        .verifyComplete();

    StepVerifier.create(ldaptiveTemplate.removeAll(List.of(p0, p1, p2), personMapper))
        .expectNext(3L)
        .verifyComplete();
  }

  /**
   * Modify dn.
   */
  @Test
  void modifyDn() {
    Person p0 = new Person();
    p0.setCn("Musil");
    p0.setSn("Robert");
    p0.setUid("musil");

    StepVerifier.create(ldaptiveTemplate.save(p0, personMapper))
        .assertNext(pr -> StepVerifier
            .create(ldaptiveTemplate.modifyDn(ModifyDnRequest.builder()
                .oldDN(personMapper.mapDn(p0))
                .newRDN("uid=mrt")
                .delete(true)
                .build()))
            .assertNext(r -> assertTrue(r.isSuccess()))
            .verifyComplete()
        )
        .verifyComplete();
    assertNotNull(p0);

    StepVerifier.create(ldaptiveTemplate
            .findOne(SearchRequest.builder().filter("(uid=mrt)").build()))
        .assertNext(ldapEntry -> {
          assertEquals("mrt", ldapEntry.getAttribute("uid").getStringValue());
          StepVerifier.create(ldaptiveTemplate.delete(DeleteRequest.builder().dn(ldapEntry.getDn()).build()))
              .assertNext(r -> assertTrue(r.isSuccess()))
              .verifyComplete();
        })
        .verifyComplete();
  }

  /**
   * Test clone.
   */
  @Test
  void testClone() {
    ReactiveLdaptiveTemplate clone = ldaptiveTemplate.clone();
    assertNotNull(clone);
    clone = ldaptiveTemplate.clone(new DefaultLdaptiveErrorHandler());
    assertNotNull(clone);
  }

}

