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
import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.getAttributeValuesAsSet;
import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.setAttribute;
import static org.bremersee.data.ldaptive.LdaptiveEntryMapper.setAttributes;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.bremersee.data.ldaptive.LdaptiveEntryMapper;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.io.StringValueTranscoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The test group mapper.
 *
 * @author Christian Bremer
 */
@Component
@Validated
public class GroupMapper implements LdaptiveEntryMapper<Group> {

  private static final StringValueTranscoder STRING_TRANSCODER = new StringValueTranscoder();

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  private String getBaseDn() {
    return "ou=groups," + baseDn;
  }

  @Override
  public String[] getObjectClasses() {
    return new String[]{"top", "groupOfUniqueNames"};
  }

  @Override
  public String mapDn(Group group) {
    if (group == null || !StringUtils.hasText(group.getCn())) {
      return null;
    }
    return createDn("cn", group.getCn(), getBaseDn());
  }

  @Override
  public Group map(LdapEntry ldapEntry) {
    if (ldapEntry == null) {
      return null;
    }
    Group group = new Group();
    map(ldapEntry, group);
    return group;
  }

  @Override
  public void map(LdapEntry ldapEntry, Group group) {
    group.setCn(getAttributeValue(ldapEntry,
        "cn", STRING_TRANSCODER, null));
    group.setOu(getAttributeValue(ldapEntry,
        "ou", STRING_TRANSCODER, null));
    group.setMembers(getAttributeValuesAsSet(ldapEntry,
        "uniqueMember", STRING_TRANSCODER));
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(@NotNull Group source,
      @NotNull LdapEntry destination) {
    List<AttributeModification> modifications = new ArrayList<>();
    setAttribute(destination, "cn", source.getCn(), false, STRING_TRANSCODER, modifications);
    setAttribute(destination, "ou", source.getOu(), false, STRING_TRANSCODER, modifications);
    setAttributes(destination, "uniqueMember", source.getMembers(), false, STRING_TRANSCODER,
            modifications);
    return modifications.toArray(new AttributeModification[0]);
  }

}
