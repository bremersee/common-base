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

package org.bremersee.web.reactive.multipart;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.bremersee.web.multipart.FileAwareMultipartFile;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The multipart file builder interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface MultipartFileBuilder {

  /**
   * Build multipart file from the given part.
   *
   * <p>If hhe given part is {@code null}, an empty multipart file will be returned.
   *
   * @param part the part (can be {@code null})
   * @return the multipart file
   */
  Mono<MultipartFile> build(Part part);

  /**
   * Build multipart file from the last part of the given parts.
   *
   * @param parts the parts
   * @return the multipart file
   */
  Mono<MultipartFile> build(@NotNull Flux<? extends Part> parts);

  /**
   * Build multipart files from the given parts.
   *
   * @param parts the parts
   * @return the multipart files
   */
  Mono<List<MultipartFile>> buildList(@NotNull Flux<? extends Part> parts);

  /**
   * Build list of multipart files from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param requestParameters the request parameters
   * @return the list of multipart files
   */
  Mono<List<MultipartFile>> buildList(
      @NotNull MultiValueMap<String, Part> multiPartData,
      String... requestParameters);

  /**
   * Build list (flux) of lists of multipart files from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param requestParameters the request parameters
   * @return the list (flux) of lists of multipart files
   */
  Flux<List<MultipartFile>> buildLists(
      @NotNull MultiValueMap<String, Part> multiPartData,
      String... requestParameters);

  /**
   * Build map mono.
   *
   * @param parts the parts
   * @return the mono
   */
  @SuppressWarnings("unchecked")
  Mono<Map<String, MultipartFile>> buildMap(@NotNull Flux<? extends Part>... parts);

  /**
   * Build map of multipart files from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param requestParameters the request parameters
   * @return the map of multipart files
   */
  Mono<Map<String, MultipartFile>> buildMap(
      @NotNull MultiValueMap<String, Part> multiPartData,
      String... requestParameters);

  /**
   * Build multi value map mono.
   *
   * @param parts the parts
   * @return the mono
   */
  @SuppressWarnings("unchecked")
  Mono<MultiValueMap<String, MultipartFile>> buildMultiValueMap(
      @NotNull Flux<? extends Part>... parts);

  /**
   * Build multi value map of multipart files from the given multi part data.
   *
   * @param multiPartData the multi part data
   * @param requestParameters the request parameters
   * @return the multi value map of multipart files
   */
  Mono<MultiValueMap<String, MultipartFile>> buildMultiValueMap(
      @NotNull MultiValueMap<String, Part> multiPartData,
      String... requestParameters);

  /**
   * Get multipart file with the specified index. If the list is smaller than the index, an empty multipart file will be
   * returned.
   *
   * @param list the list
   * @param index the index
   * @return the multipart file
   */
  @NotNull
  static MultipartFile getMultipartFile(List<MultipartFile> list, int index) {
    MultipartFile multipartFile = list != null && index >= 0 && list.size() > index
        ? list.get(index)
        : FileAwareMultipartFile.empty();
    return multipartFile != null ? multipartFile : FileAwareMultipartFile.empty();
  }

  /**
   * Get multipart file with the specified request parameter name. If no such multipart file exists, an empty one will
   * be returned.
   *
   * @param map the map
   * @param name the request parameter name
   * @return the multipart file
   */
  @NotNull
  static MultipartFile getMultipartFile(Map<String, MultipartFile> map, String name) {
    MultipartFile multipartFile = map != null && name != null
        ? map.getOrDefault(name, FileAwareMultipartFile.empty())
        : FileAwareMultipartFile.empty();
    return multipartFile != null ? multipartFile : FileAwareMultipartFile.empty();
  }

  /**
   * Get multipart files with the specified request parameter. If no such list of multipart files exists, an empty list
   * will be returned.
   *
   * @param map the map
   * @param name the name
   * @return the multipart files
   */
  @NotNull
  static List<MultipartFile> getMultipartFiles(
      MultiValueMap<String, MultipartFile> map,
      String name) {

    List<MultipartFile> list = map != null && name != null
        ? map.getOrDefault(name, Collections.emptyList())
        : Collections.emptyList();
    return list != null ? list : Collections.emptyList();
  }

  /**
   * Get first multipart file. If no such multipart file exists, an empty one will be returned.
   *
   * @param map the map
   * @param name the name
   * @return the first multipart file
   */
  @NotNull
  static MultipartFile getFirstMultipartFile(
      MultiValueMap<String, MultipartFile> map,
      String name) {

    MultipartFile multipartFile = map != null && name != null
        ? map.getFirst(name)
        : FileAwareMultipartFile.empty();
    return multipartFile != null ? multipartFile : FileAwareMultipartFile.empty();
  }

}
