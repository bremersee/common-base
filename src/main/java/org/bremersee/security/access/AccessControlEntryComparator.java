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

package org.bremersee.security.access;

import java.util.Comparator;
import org.bremersee.common.model.AccessControlEntry;

/**
 * The access control entry comparator.
 *
 * @author Christian Bremer
 */
public class AccessControlEntryComparator implements Comparator<AccessControlEntry> {

  @Override
  public int compare(final AccessControlEntry o1, final AccessControlEntry o2) {
    final String s1 = o1 != null && o1.getPermission() != null ? o1.getPermission() : "";
    final String s2 = o2 != null && o2.getPermission() != null ? o2.getPermission() : "";
    return s1.compareToIgnoreCase(s2);
  }
}
