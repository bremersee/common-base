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
import org.bremersee.geojson.model.Geometry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Bad apis for testing.
 *
 * @author Christian Bremer
 */
public class BadApis {

  /**
   * The interface One.
   */
  @Api(value = "BadApiController")
  public interface One {
  }

  /**
   * The interface Two.
   */
  public interface Two {
  }


  /**
   * The interface Three.
   */
  @Api(value = "BadApiController")
  public interface Three {

    /**
     * Gets geometries.
     *
     * @param query the query
     * @return the geometries
     */
    @RequestMapping(
        value = "/api/geometries",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Geometry>> getGeometries(
        @ApiParam(value = "The query.") @RequestParam(name = "q", required = false) String query);

    /**
     * Update geometry response entity.
     *
     * @param id the id
     * @param geometry the geometry
     * @return the response entity
     */
    @PutMapping(path = "/api/geometries/{id}")
    ResponseEntity<Void> updateGeometry(
        @PathVariable("id") String id,
        @RequestBody Geometry geometry);

    /**
     * Add geometry response entity.
     *
     * @param geometry the geometry
     * @return the response entity
     */
    @PostMapping(path = "/api/geometries")
    ResponseEntity<Void> addGeometry(
        @RequestBody Geometry geometry);

    /**
     * Gets geometry.
     *
     * @param id the id
     * @return the geometry
     */
    @ApiOperation(
        value = "Get geometry by ID.",
        nickname = "getGeometry",
        response = Geometry.class,
        tags = {"geometry-controller"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Geometry.class)
    })
    @GetMapping(path = "/api/geometries/{id}")
    ResponseEntity<Geometry> getGeometry(@PathVariable("id") String id);
  }

  /**
   * The interface Four.
   */
  @Api(value = "BadApiController")
  public interface Four {

    /**
     * Update geometry response entity.
     *
     * @param geometry the geometry
     * @return the response entity
     */
    @SuppressWarnings("MVCPathVariableInspection")
    @PutMapping(path = "/api/geometries/{id}")
    ResponseEntity<Void> updateGeometry(
        @RequestBody Geometry geometry);

    /**
     * Add geometry response entity.
     *
     * @param geometry the geometry
     * @return the response entity
     */
    @SuppressWarnings("unused")
    ResponseEntity<Void> addGeometry(
        @RequestBody Geometry geometry);

    /**
     * Gets geometry.
     *
     * @param id the id
     * @return the geometry
     */
    @ApiOperation(
        value = "Get geometry by ID.",
        nickname = "getGeometry",
        response = String.class,
        tags = {"geometry-controller"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class)
    })
    @GetMapping(path = "/api/geometries/{id}")
    ResponseEntity<String> getGeometry(@PathVariable("id") String id);
  }


  /**
   * The interface Five.
   */
  @Api(value = "BadApiController")
  public interface Five {

    /**
     * Gets geometries.
     *
     * @param query the query
     * @return the geometries
     */
    @RequestMapping(
        value = "/api/geometries",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Geometry>> getGeometries(
        @ApiParam(value = "The query.") @RequestParam(name = "q", required = false) String query);
  }

  /**
   * The interface Six.
   */
  @Api(value = "BadApiController")
  public interface Six {

    /**
     * Gets geometries.
     *
     * @param query the query
     * @return the geometries
     */
    @GetMapping(
        value = "/api/geometries",
        produces = {"application/json"})
    ResponseEntity<List<Geometry>> getGeometries(
        @ApiParam(value = "The query.") @RequestParam(name = "q", required = false) String query);
  }


  /**
   * The interface Seven.
   */
  @Api(value = "BadApiController")
  public interface Seven {

    /**
     * Gets geometries.
     *
     * @param query the query
     * @return the geometries
     */
    @RequestMapping(
        value = "/api/geometries",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Geometry>> getGeometries(
        @ApiParam(value = "The query.") @RequestParam(name = "q", required = false) String query);
  }

  /**
   * The interface Eight.
   */
  @Api(value = "BadApiController")
  public interface Eight {

    /**
     * Gets geometries.
     *
     * @param query the query
     * @return the geometries
     */
    @RequestMapping(
        value = "/api/geometries",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Geometry>> getGeometries(
        @RequestParam(name = "q", required = false) String query);
  }


}
