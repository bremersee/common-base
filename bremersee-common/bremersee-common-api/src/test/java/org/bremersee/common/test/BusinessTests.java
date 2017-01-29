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
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.business.RoleNameServiceImpl;
import org.junit.Test;

/**
 * @author Christian Bremer
 */
public class BusinessTests {

    static final String VEHICLE_ENTITY_TYPE = "Vehicle";

    RoleNameService roleNameService = new RoleNameServiceImpl();

    @Test
    public void testCustomRoleName() {
        final String userName = "foo@example.org";
        System.out.println("Testing custom role name ...");
        final String roleName = roleNameService.generateCustomRoleName(userName);
        TestCase.assertTrue(roleNameService.isCustomRoleName(roleName, null));
        TestCase.assertTrue(roleNameService.isCustomRoleName(roleName, userName));
        TestCase.assertEquals(userName, roleNameService.getUserNameFromCustomRoleName(roleName));
        System.out.println("Testing custom role name ... DONE!");
    }

    @Test
    public void testFriendsRoleName() {
        System.out.println("Testing friends role name ...");
        final String user = "junit-tester";
        String roleName = roleNameService.createFriendsRoleName(user);
        TestCase.assertTrue(roleNameService.isFriendsRoleName(roleName));
        TestCase.assertEquals(user, roleNameService.getUserNameFromFriendsRoleName(roleName));
        System.out.println("Testing friends role name ... DONE!");
    }

    @Test
    public void testOwnerRoleName() {
        System.out.println("Testing owner role name ...");
        final String entityId = "aa12bb34";
        String roleName = roleNameService.createOwnerRoleName(VEHICLE_ENTITY_TYPE, entityId);
        TestCase.assertTrue(roleNameService.isOwnerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        TestCase.assertEquals(entityId,
                roleNameService.getEntityIdFromOwnerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        System.out.println("Testing owner role name ... DONE!");
    }

    @Test
    public void testManagerRoleName() {
        System.out.println("Testing manager role name ...");
        final String entityId = "aa12bb34";
        String roleName = roleNameService.createManagerRoleName(VEHICLE_ENTITY_TYPE, entityId);
        TestCase.assertTrue(roleNameService.isManagerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        TestCase.assertEquals(entityId,
                roleNameService.getEntityIdFromManagerRoleName(roleName, VEHICLE_ENTITY_TYPE));
        System.out.println("Testing manager role name ... DONE!");
    }

}
