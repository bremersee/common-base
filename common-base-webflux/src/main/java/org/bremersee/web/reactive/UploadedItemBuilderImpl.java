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

package org.bremersee.web.reactive;

import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.web.ReqParam;
import org.bremersee.web.UploadedByteArray;
import org.bremersee.web.UploadedFile;
import org.bremersee.web.UploadedItem;
import org.springframework.http.MediaType;
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
 * The reactive uploaded item builder implementation.
 *
 * @author Christian Bremer
 */
@Slf4j
public class UploadedItemBuilderImpl implements UploadedItemBuilder {

  private static final Path DEFAULT_TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

  private static final String MISSING_REQUIRED_PART_ERROR_CODE = "MULTIPART_PARAMETER_IS_REQUIRED";

  private static final String MAX_SIZE_EXCEEDED_ERROR_CODE = "MULTIPART_SIZE_IS_EXCEEDED";

  private static final String ILLEGAL_CONTENT_TYPE_ERROR_CODE = "MULTIPART_CONTENT_TYPE_IS_ILLEGAL";

  private final Path tmpDir;

  /**
   * Instantiates a new reactive uploaded item builder implementation.
   */
  public UploadedItemBuilderImpl() {
    this(DEFAULT_TMP_DIR);
  }

  /**
   * Instantiates a new reactive uploaded item builder implementation.
   *
   * @param tmpDir the tmp dir
   */
  public UploadedItemBuilderImpl(String tmpDir) {
    this(StringUtils.hasText(tmpDir) ? Paths.get(tmpDir) : DEFAULT_TMP_DIR);
  }

  /**
   * Instantiates a new reactive uploaded item builder implementation.
   *
   * @param tmpDir the tmp dir
   */
  public UploadedItemBuilderImpl(Path tmpDir) {
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
   * Instantiates a new reactive uploaded item builder implementation.
   *
   * @param tmpDir the tmp dir
   */
  public UploadedItemBuilderImpl(File tmpDir) {
    this(tmpDir != null ? tmpDir.toPath() : DEFAULT_TMP_DIR);
  }

  @Override
  public Mono<UploadedItem<?>> build(Part contentPart) {
    return build(contentPart, null);
  }

  public Mono<UploadedItem<?>> build(Part contentPart, ReqParam reqParam) {
    if (contentPart instanceof FilePart) {
      return create((FilePart) contentPart, reqParam);
    }
    if (contentPart instanceof FormFieldPart) {
      return create((FormFieldPart) contentPart, reqParam);
    }
    return Mono.just(UploadedItem.EMPTY);
  }

  @Override
  public Mono<List<UploadedItem<?>>> buildFromFirstParameterValue(
      MultiValueMap<String, Part> multiPartData,
      ReqParam... requestParameters) {

    return Flux.fromArray(Optional.ofNullable(requestParameters).orElseGet(() -> new ReqParam[0]))
        .flatMap(requestParameter -> build(
            findPart(multiPartData, requestParameter), requestParameter))
        .collectList();
  }

  @Override
  public Flux<List<UploadedItem<?>>> buildFromAllParameterValues(
      MultiValueMap<String, Part> multiPartData,
      ReqParam... requestParameters) {

    return Flux.fromArray(Optional.ofNullable(requestParameters).orElseGet(() -> new ReqParam[0]))
        .flatMap(reqParam -> createFromAllParameterValues(multiPartData, reqParam));
  }

  @Override
  public Mono<Map<String, UploadedItem<?>>> buildMapFromFirstParameterValue(
      MultiValueMap<String, Part> multiPartData,
      ReqParam... requestParameters) {

    return Flux.fromArray(Optional.ofNullable(requestParameters).orElseGet(() -> new ReqParam[0]))
        .flatMap(reqParam -> build(findPart(multiPartData, reqParam), reqParam)
            .zipWith(Mono.just(reqParam.getName())))
        .collectMap(Tuple2::getT2, Tuple2::getT1, LinkedHashMap::new);
  }

  @Override
  public Mono<MultiValueMap<String, UploadedItem<?>>> buildMapFromAllParameterValues(
      MultiValueMap<String, Part> multiPartData,
      ReqParam... requestParameters) {

    return Flux.fromArray(Optional.ofNullable(requestParameters).orElseGet(() -> new ReqParam[0]))
        .flatMap(reqParam -> createFromAllParameterValues(multiPartData, reqParam)
            .zipWith(Mono.just(reqParam.getName())))
        .collectMap(Tuple2::getT2, Tuple2::getT1, LinkedHashMap::new)
        .map(LinkedMultiValueMap::new);
  }

  private Mono<UploadedItem<?>> create(FilePart part, ReqParam reqParam) {
    if (part == null) {
      return Mono.just(UploadedItem.EMPTY);
    }
    final Path file = getTmpFile();
    return part.transferTo(file)
        .then(Mono.just(new UploadedFile(file, findContentType(part), findFilename(part))))
        .map(uploadedFile -> validate(uploadedFile, reqParam));
  }

  private Mono<UploadedItem<?>> create(FormFieldPart part, ReqParam reqParam) {
    if (part == null) {
      return Mono.just(UploadedItem.EMPTY);
    }
    return Mono.just(part)
        .map(formFieldPart -> {
          String value = formFieldPart.value();
          byte[] data = null;
          String contentType = null;
          if (value.toLowerCase().startsWith("data:") && value.indexOf(',') > -1) {
            DataUrl dataUrl = null;
            try {
              dataUrl = new DataUrlSerializer().unserialize(value);
            } catch (Exception e) {
              log.debug("Parsing form field as data url failed, "
                  + "treating value as plain/text (value = {}).", value);
            }
            if (dataUrl != null) {
              data = dataUrl.getData();
              contentType = dataUrl.getMimeType();
            }
          }
          if (data == null) {
            data = value.getBytes(StandardCharsets.UTF_8);
            contentType = MediaType.TEXT_PLAIN_VALUE;
          }
          UploadedItem<?> item = new UploadedByteArray(data, contentType, null);
          return validate(item, reqParam);
        });
  }

  private UploadedItem<?> validate(UploadedItem<?> item, ReqParam reqParam) {
    if (reqParam == null) {
      return item;
    }
    if (reqParam.getMaxSize() > 0 && item.getLength() > reqParam.getMaxSize()) {
      item.delete();
      throw buildMaxSizeExceededException(reqParam.getName(), reqParam.getMaxSize());
    }
    MediaType uploadedMediaType = StringUtils.hasText(item.getContentType())
        ? MediaType.parseMediaType(item.getContentType())
        : MediaType.APPLICATION_OCTET_STREAM;
    for (String allowedContentType : reqParam.getAllowedMediaTypes()) {
      if (MediaType.parseMediaType(allowedContentType).includes(uploadedMediaType)) {
        return item;
      }
    }
    item.delete();
    throw buildIllegalContentTypeException(reqParam.getName(), reqParam.getAllowedMediaTypes());
  }

  private Mono<List<UploadedItem<?>>> createFromAllParameterValues(
      MultiValueMap<String, Part> multiPartData,
      ReqParam requestParameter) {

    List<Part> contentParts = multiPartData
        .getOrDefault(requestParameter.getName(), Collections.emptyList());
    if (requestParameter.isRequired() && contentParts.isEmpty()) {
      return Mono.error(() -> buildMissingRequiredPartException(requestParameter.getName()));
    }
    return Flux.fromIterable(contentParts)
        .flatMap(part -> build(part, requestParameter))
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

  private Part findPart(MultiValueMap<String, Part> multiPartData, ReqParam requestParameter) {
    return Optional.ofNullable(requestParameter)
        .map(key -> {
          Part part = multiPartData.getFirst(requestParameter.getName());
          if (requestParameter.isRequired() && part == null) {
            throw buildMissingRequiredPartException(requestParameter.getName());
          }
          return part;
        })
        .orElse(null);
  }

  private Path getTmpFile() {
    try {
      return Files.createTempFile(tmpDir, "obuib-", ".tmp");
    } catch (Exception e) {
      throw ServiceException.internalServerError("Creating tmp file failed.", e);
    }
  }

  // TODO job that deletes tmp files

  /**
   * Build missing required part exception.
   *
   * @param partName the part name
   * @return the service exception
   */
  private static ServiceException buildMissingRequiredPartException(String partName) {
    return ServiceException.badRequest(
        "Parameter '" + partName + "' of multipart request is required.",
        MISSING_REQUIRED_PART_ERROR_CODE + ":" + partName);
  }

  /**
   * Build max size exceeded exception service exception.
   *
   * @param partName the part name
   * @param maxSize the max size
   * @return the service exception
   */
  private static ServiceException buildMaxSizeExceededException(String partName, long maxSize) {
    final String max;
    if (maxSize > 1024 * 1024 * 1024) {
      max = (maxSize / (1024 * 1024 * 1024)) + " GB";
    } else if (maxSize > 1024 * 1024) {
      max = (maxSize / (1024 * 1024)) + " MB";
    } else if (maxSize > 1024) {
      max = (maxSize / 1024) + " KB";
    } else {
      max = maxSize + " Bytes";
    }
    return ServiceException.badRequest(
        "The maximum size (" + max + ") of the multipart '"
            + partName + "' is exceeded.",
        MAX_SIZE_EXCEEDED_ERROR_CODE + ":" + partName);
  }

  /**
   * Build illegal content type exception service exception.
   *
   * @param partName the part name
   * @param allowedContentType the allowed content type
   * @return the service exception
   */
  private static ServiceException buildIllegalContentTypeException(
      String partName,
      String[] allowedContentType) {
    return ServiceException.badRequest(
        "Multipart '" + partName + "' has an illegal content type. It must be one of "
            + Arrays.toString(allowedContentType) + ".",
        ILLEGAL_CONTENT_TYPE_ERROR_CODE + ":" + partName);
  }

}
