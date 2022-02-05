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

package org.bremersee.http.codec.xml.model;

import java.util.Collection;
import java.util.List;
import org.bremersee.xml.JaxbContextData;
import org.bremersee.xml.JaxbContextDataProvider;

/**
 * The example jaxb context data provider for the tests.
 *
 * @author Christian Bremer
 */
public class XmlTestJaxbContextDataProvider implements JaxbContextDataProvider {

  @Override
  public Collection<JaxbContextData> getJaxbContextData() {
    return List.of(
        new JaxbContextData(
            org.bremersee.http.codec.xml.model.xml2.ObjectFactory.class.getPackage(),
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"),
        new JaxbContextData(
            org.bremersee.http.codec.xml.model.xml3.ObjectFactory.class.getPackage()),
        new JaxbContextData(
            org.bremersee.http.codec.xml.model.xml4.ObjectFactory.class.getPackage())
    );
  }
}
