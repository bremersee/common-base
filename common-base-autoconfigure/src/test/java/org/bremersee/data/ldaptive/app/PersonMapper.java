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

package org.bremersee.data.ldaptive.app;

import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.createDn;
import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.setAttribute;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.bremersee.data.ldaptive.LdaptiveEntryMapper;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.transcode.StringValueTranscoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The test person mapper.
 *
 * @author Christian Bremer
 */
@Component
@Validated
public class PersonMapper implements LdaptiveEntryMapper<Person> {

  private static final StringValueTranscoder STRING_TRANSCODER = new StringValueTranscoder();

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  private String getBaseDn() {
    return "ou=people," + baseDn;
  }

  @Override
  public String[] getObjectClasses() {
    return new String[]{"top", "person", "organizationalPerson", "inetOrgPerson"};
  }

  @Override
  public String mapDn(Person person) {
    if (person == null || !StringUtils.hasText(person.getUid())) {
      return null;
    }
    return createDn("uid", person.getUid(), getBaseDn());
  }

  @Override
  public Person map(LdapEntry ldapEntry) {
    if (ldapEntry == null) {
      return null;
    }
    Person person = new Person();
    map(ldapEntry, person);
    return person;
  }

  @Override
  public void map(LdapEntry ldapEntry, Person person) {
    person.setCn(getAttributeValue(ldapEntry,
        "cn", STRING_TRANSCODER, null));
    person.setUid(getAttributeValue(ldapEntry,
        "uid", STRING_TRANSCODER, null));
    person.setSn(getAttributeValue(ldapEntry,
        "sn", STRING_TRANSCODER, null));
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      @NotNull Person source,
      @NotNull LdapEntry destination) {
    List<AttributeModification> modifications = new ArrayList<>();
    setAttribute(destination, "uid", source.getUid(), false, STRING_TRANSCODER, modifications);
    setAttribute(destination, "cn", source.getCn(), false, STRING_TRANSCODER, modifications);
    setAttribute(destination, "sn", source.getSn(), false, STRING_TRANSCODER, modifications);
    return modifications.toArray(new AttributeModification[0]);
  }

}
