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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import org.bremersee.geojson.model.Geometry;
import org.junit.jupiter.api.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Rest api one for testing.
 *
 * @author Christian Bremer
 */
@Tag(value = "GoodRestApiController")
@Validated
public interface GoodRestApiOne {

  /**
   * Gets geometries.
   *
   * @param query the query
   * @return the geometries
   */
  @Operation(
      summary = "Get geometries.",
      operationId = "getGeometries",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "OK",
          content = @Content(
              array = @ArraySchema(
                  schema = @Schema(implementation = Geometry.class)))),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden")
  })
  @RequestMapping(
      value = "/api/geometries",
      produces = {"application/json"},
      method = RequestMethod.GET)
  ResponseEntity<List<Geometry>> getGeometries(
      @Parameter(description = "The query.")
      @RequestParam(name = "q", required = false) String query);

  /**
   * Add geometry response entity.
   *
   * @param geometry the geometry
   * @return the response entity
   */
  @Operation(
      summary = "Add geometry.",
      operationId = "addGeometry",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "OK",
          content = @Content(schema = @Schema(implementation = Geometry.class))),
      @ApiResponse(
          responseCode = "400",
          description = "Bad Request",
          content = @Content(schema = @Schema(
              implementation = org.bremersee.exception.model.RestApiException.class))),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden")
  })
  @RequestMapping(
      value = "/api/geometries",
      produces = {"application/json"},
      consumes = {"application/json"},
      method = RequestMethod.POST)
  ResponseEntity<Geometry> addGeometry(
      @Parameter(description = "The geometry.", required = true)
      @Valid @RequestBody Geometry geometry);

  /**
   * Gets geometry.
   *
   * @param id the id
   * @return the geometry
   */
  @Operation(
      description = "Get geometry.",
      operationId = "getGeometry",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "OK",
          content = @Content(schema = @Schema(implementation = Geometry.class))),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden"),
      @ApiResponse(
          responseCode = "404",
          description = "Not Found",
          content = @Content(schema = @Schema(
              implementation = org.bremersee.exception.model.RestApiException.class)))
  })
  @RequestMapping(
      value = "/api/geometries/{id}",
      produces = {"application/json"},
      method = RequestMethod.GET)
  ResponseEntity<Geometry> getGeometry(
      @Parameter(description = "The geometry ID.", required = true) @PathVariable("id") String id);

  /**
   * Update geometry response entity.
   *
   * @param id the id
   * @param geometry the geometry
   * @return the response entity
   */
  @Operation(
      summary = "Update geometry.",
      operationId = "updateGeometry",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "OK",
          content = @Content(schema = @Schema(implementation = Geometry.class))),
      @ApiResponse(
          responseCode = "400",
          description = "Bad Request",
          content = @Content(schema = @Schema(
              implementation = org.bremersee.exception.model.RestApiException.class))),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden"),
      @ApiResponse(
          responseCode = "404",
          description = "Not Found",
          content = @Content(schema = @Schema(
              implementation = org.bremersee.exception.model.RestApiException.class)))
  })
  @RequestMapping(
      value = "/api/geometries/{id}",
      produces = {"application/json"},
      consumes = {"application/json"},
      method = RequestMethod.PUT)
  ResponseEntity<Geometry> updateGeometry(
      @Parameter(description = "The geometry ID.", required = true) @PathVariable("id") String id,
      @Parameter(description = "The geometry.", required = true)
      @Valid @RequestBody Geometry geometry);

  /**
   * Delete geometry response entity.
   *
   * @param id the id
   * @return the response entity
   */
  @Operation(
      summary = "Delete geometry.",
      operationId = "deleteGeometry",
      tags = {"geometry-controller"})
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "OK"),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden"),
      @ApiResponse(
          responseCode = "404",
          description = "Not Found",
          content = @Content(schema = @Schema(
              implementation = org.bremersee.exception.model.RestApiException.class)))
  })
  @RequestMapping(
      value = "/api/geometries/{id}",
      produces = {"application/json"},
      method = RequestMethod.DELETE)
  ResponseEntity<Void> deleteGeometry(
      @Parameter(description = "The geometry ID.", required = true) @PathVariable("id") String id);

}
