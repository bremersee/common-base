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

package org.bremersee.http.codec.xml.app;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.http.codec.xml.model.xml1.Person;
import org.bremersee.http.codec.xml.model.xml2.Vehicle;
import org.bremersee.http.codec.xml.model.xml2.Vehicles;
import org.bremersee.http.codec.xml.model.xml3.Company;
import org.bremersee.http.codec.xml.model.xml4.Address;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * The xml controller.
 *
 * @author Christian Bremer
 */
@RestController
@Slf4j
public class XmlController {

  /**
   * Post person flux.
   *
   * @param model the model
   * @return the flux
   */
  @PostMapping(
      path = "/person",
      consumes = MediaType.APPLICATION_XML_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public Flux<Person> postPerson(@RequestBody Person model) {
    log.info("Echo person = {}", model);
    return Flux.just(model);
  }

  /**
   * Post vehicle flux.
   *
   * @param model the model
   * @return the flux
   */
  @PostMapping(
      path = "/vehicle",
      consumes = MediaType.APPLICATION_XML_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public Flux<Vehicle> postVehicle(@RequestBody Vehicle model) {
    log.info("Echo vehicle = {}", model);
    return Flux.just(model);
  }

  /**
   * Post vehicles flux.
   *
   * @param model the model
   * @return the flux
   */
  @PostMapping(
      path = "/vehicles",
      consumes = MediaType.APPLICATION_XML_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public Flux<Vehicles> postVehicles(@RequestBody Vehicles model) {
    log.info("Echo vehicles = {}", model);
    return Flux.just(model);
  }

  /**
   * Post company flux.
   *
   * @param model the model
   * @return the flux
   */
  @PostMapping(
      path = "/company",
      consumes = MediaType.APPLICATION_XML_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public Flux<Company> postCompany(@RequestBody Company model) {
    log.info("Echo company = {}", model);
    return Flux.just(model);
  }

  /**
   * Post address flux.
   *
   * @param model the model
   * @return the flux
   */
  @PostMapping(
      path = "/address",
      consumes = MediaType.APPLICATION_XML_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public Flux<Address> postAddress(@RequestBody Address model) {
    log.info("Echo address = {}", model);
    return Flux.just(model);
  }

}
