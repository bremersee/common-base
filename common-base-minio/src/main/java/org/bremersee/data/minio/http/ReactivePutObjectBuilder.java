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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.minio.InMemoryPutObject;
import org.bremersee.data.minio.FileSystemPutObject;
import org.bremersee.data.minio.PutObject;
import org.bremersee.exception.ServiceException;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * The reactive put object builder.
 *
 * @author Christian Bremer
 */
public interface ReactivePutObjectBuilder {

  /**
   * Build put object.
   *
   * @param contentPart the content part
   * @param contentTypePart the content type part
   * @param filenamePart the filename part
   * @return the put object
   */
  Mono<PutObject<?>> build(
      @Nullable Part contentPart,
      @Nullable Part contentTypePart,
      @Nullable Part filenamePart);

  /**
   * Build list of put objects.
   *
   * @param multiPartData the multi part data
   * @param partNames the part names
   * @return the list of put objects
   */
  Mono<List<PutObject<?>>> buildList(
      @NotNull MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames);

  /**
   * Build map of put objects.
   *
   * @param multiPartData the multi part data
   * @param partNames the part names
   * @return the map of put objects
   */
  Mono<Map<String, PutObject<?>>> buildMap(
      @NotNull MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames);

  /**
   * Gets put object.
   *
   * @param list the list
   * @param index the index
   * @return the put object
   */
  static PutObject<?> getPutObject(List<PutObject<?>> list, int index) {
    return list != null && index >= 0 && list.size() > index ? list.get(index) : PutObject.EMPTY;
  }

  /**
   * Gets put object.
   *
   * @param map the map
   * @param name the name
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
    return new Default();
  }

  /**
   * The default implementation.
   */
  @Slf4j
  class Default implements ReactivePutObjectBuilder {

    private static final Path DEFAULT_TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    private final Path tmpDir;

    /**
     * Instantiates a new default implementation.
     */
    public Default() {
      this(DEFAULT_TMP_DIR);
    }

    /**
     * Instantiates a new default implementation.
     *
     * @param tmpDir the tmp dir
     */
    public Default(String tmpDir) {
      this(StringUtils.hasText(tmpDir) ? Paths.get(tmpDir) : DEFAULT_TMP_DIR);
    }

    /**
     * Instantiates a new default implementation.
     *
     * @param tmpDir the tmp dir
     */
    public Default(Path tmpDir) {
      this.tmpDir = tmpDir != null ? tmpDir : DEFAULT_TMP_DIR;
      if (!Files.exists(this.tmpDir)) {
        try {
          Files.createDirectories(this.tmpDir);
        } catch (IOException e) {
          throw ServiceException.internalServerError("Creating tmp dir failed.", e);
        }
      }
      if (!Files.isDirectory(this.tmpDir)) {
        throw ServiceException.internalServerError(this.tmpDir + " must be a directory.");
      }
      if (!Files.isWritable(this.tmpDir)) {
        throw ServiceException.internalServerError(this.tmpDir + " must be writable.");
      }
    }

    /**
     * Instantiates a new default implementation.
     *
     * @param tmpDir the tmp dir
     */
    public Default(File tmpDir) {
      this(tmpDir != null ? tmpDir.toPath() : DEFAULT_TMP_DIR);
    }

    public Mono<List<PutObject<?>>> buildList(
        MultiValueMap<String, Part> multiPartData,
        MultipartNames... partNames) {

      if (partNames == null || partNames.length == 0) {
        return Mono.empty();
      }
      return Flux.fromArray(partNames)
          .flatMap(names -> build(
              getPart(multiPartData, names.getContentPart()),
              getPart(multiPartData, names.getContentTypePart()),
              getPart(multiPartData, names.getFilenamePart())))
          .collectList();
    }

    public Mono<Map<String, PutObject<?>>> buildMap(
        MultiValueMap<String, Part> multiPartData,
        MultipartNames... partNames) {

      if (partNames == null || partNames.length == 0) {
        return Mono.empty();
      }
      return Flux.fromArray(partNames)
          .flatMap(names -> build(
              getPart(multiPartData, names.getContentPart()),
              getPart(multiPartData, names.getContentTypePart()),
              getPart(multiPartData, names.getFilenamePart()))
              .zipWith(Mono.just(names.getContentPart())))
          .collectMap(Tuple2::getT2, Tuple2::getT1);
    }

    private Part getPart(MultiValueMap<String, Part> multiPartData, String name) {
      return Optional.ofNullable(name)
          .map(multiPartData::getFirst)
          .orElse(null);
    }

    @Override
    public Mono<PutObject<?>> build(Part contentPart, Part contentTypePart, Part filenamePart) {
      if (contentPart instanceof FilePart) {
        return build((FilePart) contentPart);
      }
      if (contentPart instanceof FormFieldPart) {
        return build((FormFieldPart) contentPart, contentTypePart, filenamePart);
      }
      return Mono.just(PutObject.EMPTY);
    }

    private Mono<PutObject<?>> build(FilePart part) {
      if (part == null) {
        return Mono.just(PutObject.EMPTY);
      }
      return Mono.just(part)
          .flatMap(filePart -> {
            File file = new File(tmpDir.toFile(), getRandomFilename());
            while (file.exists()) {
              file = new File(tmpDir.toFile(), getRandomFilename());
            }
            try {
              return filePart.transferTo(file)
                  .then(Mono.just(new FileSystemPutObject(
                      file, getContentType(filePart), getFilename(filePart))));
            } catch (Exception e) {
              if (file.exists() && !file.delete()) {
                log.warn("Temporary file [{}] was not deleted.", file);
              }
              return Mono.error(e);
            }
          });
    }

    private String getRandomFilename() {
      String t = String.valueOf(System.currentTimeMillis());
      return "put-" + UUID.randomUUID() + "-" + t.substring(t.length() - 5) + ".tmp";
    }

    private Mono<PutObject<?>> build(FormFieldPart part, Part contentType, Part name) {
      if (part == null) {
        return Mono.just(PutObject.EMPTY);
      }
      return Mono.just(part)
          .map(formFieldPart -> new InMemoryPutObject(
              formFieldPart.value(),
              false,
              getContentType(contentType),
              getFilename(name)));
    }

    private String getContentType(Part part) {
      if (part instanceof FormFieldPart) {
        return ((FormFieldPart) part).value();
      }
      return Optional.ofNullable(part)
          .map(filePart -> filePart.headers().getContentType())
          .map(MimeType::toString)
          .orElse(null);
    }

    private String getFilename(Part part) {
      if (part instanceof FormFieldPart) {
        return ((FormFieldPart) part).value();
      } else if (part instanceof FilePart) {
        FilePart filePart = (FilePart) part;
        return Optional.of(filePart)
            .map(FilePart::filename)
            .orElse(null);
      } else {
        return null;
      }
    }
  }

}
