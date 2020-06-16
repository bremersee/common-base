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

package org.bremersee.data.minio.http;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.bremersee.data.minio.PutObject;
import org.bremersee.exception.ServiceException;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The reactive put object builder.
 *
 * @author Christian Bremer
 */
public interface ReactivePutObjectBuilder {

  /**
   * The constant MISSING_REQUIRED_PART_ERROR_CODE.
   */
  String MISSING_REQUIRED_PART_ERROR_CODE = "MULTIPART_PARAMETER_IS_REQUIRED";

  /**
   * Build put object from multi parts.
   *
   * @param contentPart the multi part with the uploaded content, can be a {@code FilePart} or a
   *     {@code FormFieldPart} with a base64 coded content
   * @param contentTypePart the multi part with the content type; it will be used, if the
   *     content part is a {@code FormFieldPart}
   * @param filenamePart the multi part with the file name; it will be used, if the content part
   *     is a {@code FormFieldPart}
   * @return the put object; if the content part is {@code null}, an empty {@link PutObject} will be
   *     returned
   */
  Mono<PutObject<?>> build(
      @Nullable Part contentPart,
      @Nullable Part contentTypePart,
      @Nullable Part filenamePart);

  /**
   * Build list of put objects from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param partNames the part names
   * @return the list of put objects
   */
  Mono<List<PutObject<?>>> buildFromFirstParameterValue(
      @NotNull MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames);

  /**
   * Build list (flux) of lists of put objects from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param partNames the part names
   * @return the list (flux) of lists of put objects
   */
  Flux<List<PutObject<?>>> buildFromAllParameterValues(
      @NotNull MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames);

  /**
   * Build map of put objects from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param partNames the part names
   * @return the map of put objects
   */
  Mono<Map<String, PutObject<?>>> buildMapFromFirstParameterValue(
      @NotNull MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames);

  /**
   * Build multi value map of put objects from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param partNames the part names
   * @return the mono
   */
  Mono<MultiValueMap<String, PutObject<?>>> buildMapFromAllParameterValues(
      @NotNull MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames);

  /**
   * Gets put object with the specified index. If the list is smaller than the index, an empty
   * {@link PutObject} will be returned.
   *
   * @param list the list
   * @param index the index
   * @return the put object
   */
  static PutObject<?> getPutObject(List<PutObject<?>> list, int index) {
    return list != null && index >= 0 && list.size() > index ? list.get(index) : PutObject.EMPTY;
  }

  /**
   * Gets put object with the specified request parameter name. If no such put object exists, an
   * empty one will be returned.
   *
   * @param map the map
   * @param name the request parameter name
   * @return the put object
   */
  static PutObject<?> getPutObject(Map<String, PutObject<?>> map, String name) {
    return map != null && name != null ? map.getOrDefault(name, PutObject.EMPTY) : PutObject.EMPTY;
  }

  /**
   * Default reactive put object builder.
   *
   * @return the reactive put object builder
   */
  static ReactivePutObjectBuilder defaultBuilder() {
    return new ReactivePutObjectBuilderImpl();
  }

  /**
   * Default reactive put object builder.
   *
   * @param tmpDir the tmp dir
   * @return the reactive put object builder
   */
  static ReactivePutObjectBuilder defaultBuilder(String tmpDir) {
    return new ReactivePutObjectBuilderImpl(tmpDir);
  }

  /**
   * Default reactive put object builder.
   *
   * @param tmpDir the tmp dir
   * @return the reactive put object builder
   */
  static ReactivePutObjectBuilder defaultBuilder(Path tmpDir) {
    return new ReactivePutObjectBuilderImpl(tmpDir);
  }

  /**
   * Default reactive put object builder.
   *
   * @param tmpDir the tmp dir
   * @return the reactive put object builder
   */
  static ReactivePutObjectBuilder defaultBuilder(File tmpDir) {
    return new ReactivePutObjectBuilderImpl(tmpDir);
  }

  /**
   * Build missing required part exception.
   *
   * @param partName the part name
   * @return the service exception
   */
  static ServiceException buildMissingRequiredPartException(String partName) {
    return ServiceException.badRequest(
        "Parameter '" + partName + "' of multipart request is required.",
        MISSING_REQUIRED_PART_ERROR_CODE + ":" + partName);
  }

}
