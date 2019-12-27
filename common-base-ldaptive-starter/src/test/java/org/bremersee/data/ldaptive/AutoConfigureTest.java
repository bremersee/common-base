package org.bremersee.data.ldaptive;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.app.GroupMapper;
import org.bremersee.data.ldaptive.app.PersonMapper;
import org.bremersee.data.ldaptive.app.TestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The auto configure test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
        "spring.ldap.embedded.credential.username=uid=admin",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=classpath:schema.ldif",
        "spring.ldap.embedded.port=12389",
        "spring.ldap.embedded.validation.enabled=false",
        "bremersee.ldaptive.enabled=true",
        "bremersee.ldaptive.use-unbound-id-provider=true",
        "bremersee.ldaptive.ldap-url=ldap://localhost:12389",
        "bremersee.ldaptive.use-ssl=false",
        "bremersee.ldaptive.use-start-tls=false",
        "bremersee.ldaptive.bind-dn=uid=admin",
        "bremersee.ldaptive.bind-credential=secret",
        "bremersee.ldaptive.pooled=false"
    })
@Slf4j
public class AutoConfigureTest {

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private LdaptiveTemplate ldaptiveTemplate;

  @Autowired
  private GroupMapper groupMapper;

  @Autowired
  private PersonMapper personMapper;

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

}
