/*
 * Copyright 2015 the original author or authors.
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

package org.bremersee.common.acl.test;

import junit.framework.TestCase;
import org.bremersee.common.security.core.context.RunAsAuthentication;
import org.bremersee.common.security.core.context.RunAsUtil;
import org.bremersee.common.spring.autoconfigure.AclProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Christian Bremer
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@EnableConfigurationProperties(AclProperties.class)
public class AclServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(AclServiceTests.class);

    @Autowired
    protected AclProperties aclProperties;

    @Autowired
    MutableAclService aclService;

    @Autowired
    PermissionEvaluator permissionEvaluator;

    private String getAclAdminRole() {
        return aclProperties.getTakeOwnershipRole();
    }

    private String[] getRunAsRoles() {
        return new String[] {
                getAclAdminRole()
        };
    }

    @Test
    public void testAcl() {
        LOG.info("Testing ...");

        RunAsUtil.runAs("tester", getRunAsRoles(), () -> {
            MutableAcl acl = aclService.createAcl(new ObjectIdentityImpl("TestObject", "100"));
            acl.setOwner(new PrincipalSid("tester"));
            acl.setEntriesInheriting(false);
            acl.setParent(null);
            acl.insertAce(acl.getEntries().size(), BasePermission.READ, new PrincipalSid("friend"), true);
            acl = aclService.updateAcl(acl);
            return acl;
        });

        MutableAcl acl = (MutableAcl) aclService.readAclById(new ObjectIdentityImpl("TestObject", "100"));
        LOG.info("Acl: " + acl);

        boolean friendCanRead = permissionEvaluator.hasPermission(
                new RunAsAuthentication("friend", new String[] { "ROLE_USER" }),
                "100",
                "TestObject",
                "READ");

        LOG.info("Successful? " + friendCanRead);
        TestCase.assertEquals(true, friendCanRead);

    }

}