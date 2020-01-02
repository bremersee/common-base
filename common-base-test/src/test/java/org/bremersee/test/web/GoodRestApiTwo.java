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

package org.bremersee.test.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import org.bremersee.geojson.model.Geometry;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Rest api two for testing.
 *
 * @author Christian Bremer
 */
@Api(value = "GoodRestApiController")
@Validated
public interface GoodRestApiTwo {

  /**
   * Gets geometries.
   *
   * @param query the query
   * @return the geometries
   */
  @ApiOperation(
      value = "Get geometries.",
      nickname = "getGeometries",
      response = Geometry.class,
      responseContainer = "List",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK", response = Geometry.class,
          responseContainer = "List"),
      @ApiResponse(code = 403, message = "Forbidden")
  })
  @RequestMapping(
      value = "/api/geometries",
      produces = {"application/json"},
      method = RequestMethod.GET)
  List<Geometry> getGeometries(
      @ApiParam(value = "The query.") @RequestParam(name = "q", required = false) String query);

  /**
   * Add geometry geometry.
   *
   * @param geometry the geometry
   * @return the geometry
   */
  @ApiOperation(
      value = "Add geometry.",
      nickname = "addGeometry",
      response = Geometry.class,
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK", response = Geometry.class),
      @ApiResponse(code = 400, message = "Bad Request",
          response = org.bremersee.exception.model.RestApiException.class),
      @ApiResponse(code = 403, message = "Forbidden")
  })
  @RequestMapping(
      value = "/api/geometries",
      produces = {"application/json"},
      consumes = {"application/json"},
      method = RequestMethod.POST)
  Geometry addGeometry(
      @ApiParam(value = "The geometry.", required = true) @Valid @RequestBody Geometry geometry);

  /**
   * Gets geometry.
   *
   * @param id the id
   * @return the geometry
   */
  @ApiOperation(
      value = "Get geometry.",
      nickname = "getGeometry",
      response = Geometry.class,
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK", response = Geometry.class),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 404, message = "Not Found",
          response = org.bremersee.exception.model.RestApiException.class)
  })
  @RequestMapping(
      value = "/api/geometries/{id}",
      produces = {"application/json"},
      method = RequestMethod.GET)
  Geometry getGeometry(
      @ApiParam(value = "The geometry ID.", required = true) @PathVariable("id") String id);

  /**
   * Update geometry geometry.
   *
   * @param id       the id
   * @param geometry the geometry
   * @return the geometry
   */
  @ApiOperation(
      value = "Update geometry.",
      nickname = "updateGeometry",
      response = Geometry.class,
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK", response = Geometry.class),
      @ApiResponse(code = 400, message = "Bad Request",
          response = org.bremersee.exception.model.RestApiException.class),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 404, message = "Not Found",
          response = org.bremersee.exception.model.RestApiException.class)
  })
  @RequestMapping(
      value = "/api/geometries/{id}",
      produces = {"application/json"},
      consumes = {"application/json"},
      method = RequestMethod.PUT)
  Geometry updateGeometry(
      @ApiParam(value = "The geometry ID.", required = true) @PathVariable("id") String id,
      @ApiParam(value = "The geometry.", required = true) @Valid @RequestBody Geometry geometry);

  /**
   * Delete geometry.
   *
   * @param id the id
   */
  @ApiOperation(
      value = "Delete geometry.",
      nickname = "deleteGeometry",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "OK"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 404, message = "Not Found",
          response = org.bremersee.exception.model.RestApiException.class)
  })
  @RequestMapping(
      value = "/api/geometries/{id}",
      produces = {"application/json"},
      method = RequestMethod.DELETE)
  void deleteGeometry(
      @ApiParam(value = "The geometry ID.", required = true) @PathVariable("id") String id);

}
