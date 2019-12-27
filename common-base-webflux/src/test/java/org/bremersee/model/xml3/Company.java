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

package org.bremersee.model.xml3;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The type Company.
 *
 * @author Christian Bremer
 */
@XmlRootElement(name = "company")
@XmlType(name = "companyType")
@SuppressWarnings("unused")
public class Company {

  private String name;

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Company{"
        + "name='" + name + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Company)) {
      return false;
    }
    Company company = (Company) o;
    return Objects.equals(name, company.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
