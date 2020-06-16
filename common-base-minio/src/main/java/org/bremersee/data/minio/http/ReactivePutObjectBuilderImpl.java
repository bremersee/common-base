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

import static org.bremersee.data.minio.http.ReactivePutObjectBuilder.buildMissingRequiredPartException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.minio.FileSystemPutObject;
import org.bremersee.data.minio.InMemoryPutObject;
import org.bremersee.data.minio.PutObject;
import org.bremersee.exception.ServiceException;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * The reactive put object builder implementation.
 */
@Slf4j
public class ReactivePutObjectBuilderImpl implements ReactivePutObjectBuilder {

  private static final Path DEFAULT_TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

  private final Path tmpDir;

  /**
   * Instantiates a new reactive put object builder implementation.
   */
  public ReactivePutObjectBuilderImpl() {
    this(DEFAULT_TMP_DIR);
  }

  /**
   * Instantiates a new reactive put object builder implementation.
   *
   * @param tmpDir the tmp dir
   */
  public ReactivePutObjectBuilderImpl(String tmpDir) {
    this(StringUtils.hasText(tmpDir) ? Paths.get(tmpDir) : DEFAULT_TMP_DIR);
  }

  /**
   * Instantiates a new reactive put object builder implementation.
   *
   * @param tmpDir the tmp dir
   */
  public ReactivePutObjectBuilderImpl(Path tmpDir) {
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
   * Instantiates a new reactive put object builder implementation.
   *
   * @param tmpDir the tmp dir
   */
  public ReactivePutObjectBuilderImpl(File tmpDir) {
    this(tmpDir != null ? tmpDir.toPath() : DEFAULT_TMP_DIR);
  }

  @Override
  public Mono<PutObject<?>> build(Part contentPart, Part contentTypePart, Part filenamePart) {
    if (contentPart instanceof FilePart) {
      return create((FilePart) contentPart);
    }
    if (contentPart instanceof FormFieldPart) {
      return create((FormFieldPart) contentPart, contentTypePart, filenamePart);
    }
    return Mono.just(PutObject.EMPTY);
  }

  @Override
  public Mono<List<PutObject<?>>> buildFromFirstParameterValue(
      MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames) {

    if (partNames == null || partNames.length == 0) {
      return Mono.empty();
    }
    return Flux.fromArray(partNames)
        .flatMap(names -> build(
            findPart(multiPartData, names.getContentPart(), names.isRequired()),
            findPart(multiPartData, names.getContentTypePart(), false),
            findPart(multiPartData, names.getFilenamePart(), false)))
        .collectList();
  }

  @Override
  public Flux<List<PutObject<?>>> buildFromAllParameterValues(
      MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames) {

    if (partNames == null || partNames.length == 0) {
      return Flux.empty();
    }
    return Flux.fromArray(partNames)
        .flatMap(names -> createFromAllParameterValues(multiPartData, names));
  }

  @Override
  public Mono<Map<String, PutObject<?>>> buildMapFromFirstParameterValue(
      MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames) {

    if (partNames == null || partNames.length == 0) {
      return Mono.empty();
    }
    return Flux.fromArray(partNames)
        .flatMap(names -> build(
            findPart(multiPartData, names.getContentPart(), names.isRequired()),
            findPart(multiPartData, names.getContentTypePart(), false),
            findPart(multiPartData, names.getFilenamePart(), false))
            .zipWith(Mono.just(names.getContentPart())))
        .collectMap(Tuple2::getT2, Tuple2::getT1, LinkedHashMap::new);
  }

  @Override
  public Mono<MultiValueMap<String, PutObject<?>>> buildMapFromAllParameterValues(
      MultiValueMap<String, Part> multiPartData,
      MultipartNames... partNames) {

    if (partNames == null || partNames.length == 0) {
      return Mono.empty();
    }
    return Flux.fromArray(partNames)
        .flatMap(names -> createFromAllParameterValues(multiPartData, names)
            .zipWith(Mono.just(names.getContentPart())))
        .collectMap(Tuple2::getT2, Tuple2::getT1, LinkedHashMap::new)
        .map(LinkedMultiValueMap::new);
  }

  private Mono<PutObject<?>> create(FilePart part) {
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
                    file, findContentType(filePart), findFilename(filePart))));
          } catch (Exception e) {
            if (file.exists() && !file.delete()) {
              log.warn("Temporary file [{}] was not deleted.", file);
            }
            return Mono.error(e);
          }
        });
  }

  private Mono<PutObject<?>> create(FormFieldPart part, Part contentType, Part name) {
    if (part == null) {
      return Mono.just(PutObject.EMPTY);
    }
    return Mono.just(part)
        .map(formFieldPart -> new InMemoryPutObject(
            formFieldPart.value(),
            false,
            findContentType(contentType),
            findFilename(name)));
  }

  private Mono<List<PutObject<?>>> createFromAllParameterValues(
      MultiValueMap<String, Part> multiPartData,
      MultipartNames names) {

    List<Part> contentParts = multiPartData
        .getOrDefault(names.getContentPart(), Collections.emptyList());
    if (names.isRequired() && contentParts.isEmpty()) {
      return Mono.error(() -> buildMissingRequiredPartException(names.getContentPart()));
    }
    return Flux.fromIterable(contentParts)
        .index()
        .flatMap(tuple -> {
          int index = tuple.getT1().intValue();
          Part contentPart = tuple.getT2();
          Part contentTypePart = findPart(multiPartData, names.getContentTypePart(), index);
          Part filenamePart = findPart(multiPartData, names.getFilenamePart(), index);
          return build(contentPart, contentTypePart, filenamePart);
        })
        .collectList();
  }

  private String findContentType(Part part) {
    if (part instanceof FormFieldPart) {
      return ((FormFieldPart) part).value();
    }
    return Optional.ofNullable(part)
        .map(filePart -> filePart.headers().getContentType())
        .map(MimeType::toString)
        .orElse(null);
  }

  private String findFilename(Part part) {
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

  private Part findPart(MultiValueMap<String, Part> multiPartData, String name, boolean required) {
    return Optional.ofNullable(name)
        .map(key -> {
          Part part = multiPartData.getFirst(key);
          if (required && part == null) {
            throw buildMissingRequiredPartException(name);
          }
          return part;
        })
        .orElse(null);
  }

  private Part findPart(MultiValueMap<String, Part> multiPartData, String name, int index) {
    return Optional.ofNullable(name)
        .map(multiPartData::get)
        .map(parts -> parts.size() > index ? parts.get(index) : null)
        .orElse(null);
  }

  private String getRandomFilename() {
    String t = String.valueOf(System.currentTimeMillis());
    return "put-" + UUID.randomUUID() + "-" + t.substring(t.length() - 5) + ".tmp";
  }

}
