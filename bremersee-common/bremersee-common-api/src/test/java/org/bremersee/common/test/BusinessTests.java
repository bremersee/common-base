/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.common.test;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.business.RoleNameServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Christian Bremer
 */
@Slf4j
public class BusinessTests {

    static final String VEHICLE_ENTITY_TYPE = "Vehicle";

    RoleNameService roleNameService = new RoleNameServiceImpl(3, "ROLE_");
    
    @Test
    public void testValidNormalRoleName() {
        log.info("Testing valid normal role names ...");
        Assert.assertTrue(roleNameService.isValidNormalRoleName("ROLE_FOOBAR"));
        Assert.assertTrue(roleNameService.isValidNormalRoleName("ROLE_FOO"));
        Assert.assertTrue(roleNameService.isValidNormalRoleName("ROLE_BAR"));
        log.info("Testing valid normal role names ... DONE!");
    }

    @Test
    public void testInvalidNormalRoleName() {
        log.info("Testing invalid normal role names ...");
        Assert.assertFalse(roleNameService.isValidNormalRoleName("ROLE_FO"));
        Assert.assertFalse(roleNameService.isValidNormalRoleName("FOO"));
        Assert.assertFalse(roleNameService.isValidNormalRoleName(RoleNameService.CUSTOM_ROLE_PREFIX + "BAR"));
        Assert.assertFalse(roleNameService.isValidNormalRoleName(RoleNameService.FRIENDS_ROLE_PREFIX + "BAR"));
        Assert.assertFalse(roleNameService.isValidNormalRoleName(RoleNameService.MANAGER_ROLE_PREFIX + "BAR"));
        Assert.assertFalse(roleNameService.isValidNormalRoleName(RoleNameService.OWNER_ROLE_PREFIX + "BAR"));
        log.info("Testing invalid normal role names ... DONE!");
    }

    @Test
    public void testCustomRoleName() {
        log.info("Testing custom role name ...");
        final String userName = "foo@example.org";
        final String roleName = roleNameService.generateCustomRoleName(userName);
        TestCase.assertTrue(roleNameService.isCustomRoleName(roleName, null));
        TestCase.assertTrue(roleNameService.isCustomRoleName(roleName, userName));
        TestCase.assertEquals(userName, roleNameService.getUserNameFromCustomRoleName(roleName));
        log.info("Testing custom role name ... DONE!");
    }

    @Test
    public void testFriendsRoleName() {
        log.info("Testing friends role name ...");
        final String user = "junit-tester";
        String roleName = roleNameService.createFriendsRoleName(user);
        TestCase.assertTrue(roleNameService.isFriendsRoleName(roleName));
        TestCase.assertEquals(user, roleNameService.getUserNameFromFriendsRoleName(roleName));
        log.info("Testing friends role name ... DONE!");
    }

    @Test
    public void testOwnerRoleName() {
        log.info("Testing owner role name ...");
        final String entityId = "aa12bb34";
        String roleName = roleNameService.createOwnerRoleName(VEHICLE_ENTITY_TYPE, entityId);
        TestCase.assertTrue(roleNameService.isOwnerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        TestCase.assertEquals(entityId,
                roleNameService.getEntityIdFromOwnerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        log.info("Testing owner role name ... DONE!");
    }

    @Test
    public void testManagerRoleName() {
        log.info("Testing manager role name ...");
        final String entityId = "aa12bb34";
        String roleName = roleNameService.createManagerRoleName(VEHICLE_ENTITY_TYPE, entityId);
        TestCase.assertTrue(roleNameService.isManagerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        TestCase.assertEquals(entityId,
                roleNameService.getEntityIdFromManagerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        log.info("Testing manager role name ... DONE!");
    }

}
