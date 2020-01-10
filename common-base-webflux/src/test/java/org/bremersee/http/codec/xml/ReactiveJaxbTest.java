/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.http.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.http.codec.xml.app.WebConfiguration;
import org.bremersee.http.codec.xml.model.xml1.Person;
import org.bremersee.http.codec.xml.model.xml2.Vehicle;
import org.bremersee.http.codec.xml.model.xml2.Vehicles;
import org.bremersee.http.codec.xml.model.xml3.Company;
import org.bremersee.http.codec.xml.model.xml4.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * The reactive jaxb test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = {WebConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"security.basic.enabled=false"})
@AutoConfigureWebTestClient
@Slf4j
public class ReactiveJaxbTest {

  @Autowired
  private WebTestClient webClient;

  /**
   * Post person.
   */
  @Test
  void postPerson() {
    Person model = new Person();
    model.setFirstName("Anna Livia");
    model.setLastName("Plurabelle");
    webClient
        .post()
        .uri("/person")
        .accept(MediaType.APPLICATION_XML)
        .contentType(MediaType.APPLICATION_XML)
        .body(BodyInserters.fromValue(model))
        .exchange()
        .expectBody(Person.class)
        .value(person -> assertEquals(model, person));

  }

  /**
   * Post vehicle.
   */
  @Test
  void postVehicle() {
    Vehicle model = new Vehicle();
    model.setBrand("Seifenkiste");
    model.setModel("Suizid");
    webClient
        .post()
        .uri("/vehicle")
        .accept(MediaType.APPLICATION_XML)
        .contentType(MediaType.APPLICATION_XML)
        .body(BodyInserters.fromValue(model))
        .exchange()
        .expectBody(Vehicle.class)
        .value(vehicle -> assertEquals(model, vehicle));
  }

  /**
   * Post vehicles.
   */
  @Test
  void postVehicles() {
    Vehicle v0 = new Vehicle();
    v0.setBrand("Audi");
    v0.setModel("TT");
    Vehicle v1 = new Vehicle();
    v1.setBrand("Audi");
    v1.setModel("A4");
    Vehicles model = new Vehicles();
    model.setSeries("Some cars from Audi");
    model.setYear(2019);
    model.setMonth(5);
    model.getEntries().addAll(Arrays.asList(v0, v1));
    webClient
        .post()
        .uri("/vehicles")
        .accept(MediaType.APPLICATION_XML)
        .contentType(MediaType.APPLICATION_XML)
        .body(BodyInserters.fromValue(model))
        .exchange()
        .expectBody(Vehicles.class)
        .value(vehicles -> assertEquals(model, vehicles));
  }

  /**
   * Post company.
   */
  @Test
  void postCompany() {
    Company model = new Company();
    model.setName("Beer & Whiskey");
    webClient
        .post()
        .uri("/company")
        .accept(MediaType.APPLICATION_XML)
        .contentType(MediaType.APPLICATION_XML)
        .body(BodyInserters.fromValue(model))
        .exchange()
        .expectBody(Company.class)
        .value(company -> assertEquals(model, company));
  }

  /**
   * Post address.
   */
  @Test
  void postAddress() {
    Address model = new Address();
    model.setStreet("Main street");
    model.setStreetNumber("42");
    webClient
        .post()
        .uri("/address")
        .accept(MediaType.APPLICATION_XML)
        .contentType(MediaType.APPLICATION_XML)
        .body(BodyInserters.fromValue(model))
        .exchange()
        .expectBody(Address.class)
        .value(address -> assertEquals(model, address));
  }

}
