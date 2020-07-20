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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlBuilder;
import eu.maxschuster.dataurl.DataUrlEncoding;
import eu.maxschuster.dataurl.DataUrlSerializer;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import org.bremersee.exception.ServiceException;
import org.bremersee.web.multipart.FileAwareMultipartFile;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The multipart file builder implementation test.
 *
 * @author Christian Bremer
 */
class MultipartFileBuilderImplTest {

  /**
   * Build with null.
   */
  @Test
  void buildWithNull() {
    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    StepVerifier
        .create(builder.build((Part) null))
        .assertNext(multipartFile -> {
          assertTrue(multipartFile.isEmpty());
          assertNull(multipartFile.getContentType());
          assertNull(multipartFile.getOriginalFilename());
          assertEquals(0L, multipartFile.getSize());
        })
        .verifyComplete();
  }

  /**
   * Build with file part.
   */
  @Test
  void buildWithFilePart() {
    MultipartFileBuilder builder = new MultipartFileBuilderImpl(
        System.getProperty("java.io.tmpdir"));
    final byte[] value = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
    StepVerifier
        .create(builder.build(createFilePart(value, "file", MediaType.TEXT_PLAIN, "test.txt")))
        .assertNext(multipartFile -> {
          try {
            assertFalse(multipartFile.isEmpty());
            assertEquals("file", multipartFile.getName());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, multipartFile.getContentType());
            assertEquals("test.txt", multipartFile.getOriginalFilename());
            assertEquals(value.length, (int) multipartFile.getSize());
            assertArrayEquals(value, multipartFile.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Fatal error", e);
          } finally {
            FileAwareMultipartFile.delete(multipartFile);
          }
        })
        .verifyComplete();
  }

  /**
   * Build with form field part.
   *
   * @throws MalformedURLException the malformed url exception
   */
  @Test
  void buildWithFormFieldPart() throws MalformedURLException {
    MultipartFileBuilder builder = new MultipartFileBuilderImpl(
        new File(System.getProperty("java.io.tmpdir")));
    final byte[] value = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(value)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType(MediaType.IMAGE_PNG_VALUE)
        .build();
    StepVerifier
        .create(
            builder.build(createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "file")))
        .assertNext(multipartFile -> {
          try {
            assertFalse(multipartFile.isEmpty());
            assertEquals("file", multipartFile.getName());
            assertEquals(MediaType.IMAGE_PNG_VALUE, multipartFile.getContentType());
            assertEquals(value.length, (int) multipartFile.getSize());
            assertArrayEquals(value, multipartFile.getBytes());
          } catch (IOException e) {
            throw ServiceException.internalServerError("Fatal error", e);
          } finally {
            FileAwareMultipartFile.delete(multipartFile);
          }
        })
        .verifyComplete();
  }

  /**
   * Build from flux.
   */
  @Test
  void buildFromFlux() {
    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    final byte[] value = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
    StepVerifier.create(builder
        .build(Flux.just(createFilePart(value, "file", MediaType.TEXT_PLAIN, "test.txt"))))
        .assertNext(multipartFile -> {
          try {
            assertFalse(multipartFile.isEmpty());
            assertEquals("file", multipartFile.getName());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, multipartFile.getContentType());
            assertEquals("test.txt", multipartFile.getOriginalFilename());
            assertEquals(value.length, (int) multipartFile.getSize());
            assertArrayEquals(value, multipartFile.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Fatal error", e);
          } finally {
            FileAwareMultipartFile.delete(multipartFile);
          }
        })
        .verifyComplete();
  }

  /**
   * Build list from flux.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildListFromFlux() throws IOException {
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    Part part1 = createFilePart(content0, "part1", MediaType.IMAGE_JPEG, "img.jpg");
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    Part part2 = createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part2");

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    StepVerifier.create(builder
        .buildList(Flux.just(part1, part2)))
        .assertNext(multipartFiles -> {
          try {
            assertEquals(2, multipartFiles.size());

            MultipartFile obj0 = MultipartFileBuilder.getMultipartFile(multipartFiles, 0);
            assertNotNull(obj0);
            assertEquals("part1", obj0.getName());
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder.getMultipartFile(multipartFiles, 1);
            assertNotNull(obj1);
            assertEquals("part2", obj1.getName());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFiles.forEach(FileAwareMultipartFile::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build map from flux array.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildMapFromFluxArray() throws IOException {
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    Part part1 = createFilePart(content0, "part1", MediaType.IMAGE_JPEG, "img.jpg");
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    Part part2 = createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part2");

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    //noinspection unchecked
    StepVerifier.create(builder.buildMap(Flux.just(part1), Flux.just(part2)))
        .assertNext(multipartFileMap -> {
          try {
            assertEquals(2, multipartFileMap.size());

            MultipartFile obj0 = MultipartFileBuilder.getMultipartFile(multipartFileMap, "part1");
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder.getMultipartFile(multipartFileMap, "part2");
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFileMap.values().forEach(FileAwareMultipartFile::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build multi value map from flux array.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildMultiValueMapFromFluxArray() throws IOException {
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    Part part1 = createFilePart(content0, "part1", MediaType.IMAGE_JPEG, "img.jpg");
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    Part part2 = createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part2");

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    //noinspection unchecked
    StepVerifier.create(builder.buildMultiValueMap(Flux.just(part1), Flux.just(part2)))
        .assertNext(multipartFileMap -> {
          try {
            assertEquals(2, multipartFileMap.size());

            MultipartFile obj0 = MultipartFileBuilder
                .getFirstMultipartFile(multipartFileMap, "part1");
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder
                .getFirstMultipartFile(multipartFileMap, "part2");
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFileMap.values()
                .forEach(multipartFiles -> multipartFiles.forEach(FileAwareMultipartFile::delete));
          }
        })
        .verifyComplete();
  }

  /**
   * Build list.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildList() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.set(
        "part1",
        createFilePart(content0, "part1", MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.set(
        "part2",
        createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part2"));

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    StepVerifier.create(builder
        .buildList(multiPartData, "part1", "part2"))
        .assertNext(multipartFiles -> {
          try {
            assertEquals(2, multipartFiles.size());

            MultipartFile obj0 = MultipartFileBuilder.getMultipartFile(multipartFiles, 0);
            assertNotNull(obj0);
            assertEquals("part1", obj0.getName());
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder.getMultipartFile(multipartFiles, 1);
            assertNotNull(obj1);
            assertEquals("part2", obj1.getName());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFiles.forEach(FileAwareMultipartFile::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build lists.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildLists() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.add(
        "part",
        createFilePart(content0, "part", MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.add(
        "part",
        createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part"));

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    StepVerifier
        .create(builder.buildLists(multiPartData, "part"))
        .assertNext(multipartFiles -> {
          try {
            assertEquals(2, multipartFiles.size());

            MultipartFile obj0 = MultipartFileBuilder.getMultipartFile(multipartFiles, 0);
            assertNotNull(obj0);
            assertEquals("part", obj0.getName());
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder.getMultipartFile(multipartFiles, 1);
            assertNotNull(obj1);
            assertEquals("part", obj1.getName());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFiles.forEach(FileAwareMultipartFile::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build map.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildMap() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.set(
        "part1",
        createFilePart(content0, "part1", MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.set(
        "part2",
        createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part2"));

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    StepVerifier.create(builder
        .buildMap(multiPartData, "part1", "part2"))
        .assertNext(multipartFileMap -> {
          try {
            assertEquals(2, multipartFileMap.size());

            MultipartFile obj0 = MultipartFileBuilder.getMultipartFile(multipartFileMap, "part1");
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder.getMultipartFile(multipartFileMap, "part2");
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFileMap.values().forEach(FileAwareMultipartFile::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build multi value map.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildMultiValueMap() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.add(
        "part",
        createFilePart(content0, "part", MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.add(
        "part",
        createFormFieldPart(new DataUrlSerializer().serialize(dataUrl), "part"));

    MultipartFileBuilder builder = new MultipartFileBuilderImpl();
    StepVerifier
        .create(builder.buildMultiValueMap(multiPartData, "part"))
        .assertNext(map -> {
          List<MultipartFile> multipartFiles = MultipartFileBuilder.getMultipartFiles(map, "part");
          assertFalse(multipartFiles.isEmpty());
          try {
            assertEquals(2, multipartFiles.size());

            MultipartFile obj0 = MultipartFileBuilder.getMultipartFile(multipartFiles, 0);
            assertNotNull(obj0);
            assertEquals("part", obj0.getName());
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getSize());
            assertArrayEquals(content0, obj0.getBytes());

            MultipartFile obj1 = MultipartFileBuilder.getMultipartFile(multipartFiles, 1);
            assertNotNull(obj1);
            assertEquals("part", obj1.getName());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getSize());
            assertArrayEquals(content1, obj1.getBytes());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            multipartFiles.forEach(FileAwareMultipartFile::delete);
          }
        })
        .verifyComplete();
  }

  private FilePart createFilePart(byte[] content, String parameterName, MediaType contentType,
      String filename) {
    FilePart part = mock(FilePart.class);
    when(part.name()).thenReturn(parameterName);
    when(part.filename()).thenReturn(filename);
    when(part.transferTo(any(File.class))).then(invocationOnMock -> {
      try {
        File file = invocationOnMock.getArgument(0);
        file.deleteOnExit();
        Files.write(
            file.toPath(),
            content,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      } catch (Exception e) {
        return Mono.error(e);
      }
      return Mono.empty();
    });
    when(part.transferTo(any(Path.class))).then(invocationOnMock -> {
      try {
        Path file = invocationOnMock.getArgument(0);
        file.toFile().deleteOnExit();
        Files.write(
            file,
            content,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      } catch (Exception e) {
        return Mono.error(e);
      }
      return Mono.empty();
    });
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(contentType);
    when(part.headers()).thenReturn(httpHeaders);
    return part;
  }

  private FormFieldPart createFormFieldPart(String content, String parameterName) {
    FormFieldPart part = mock(FormFieldPart.class);
    when(part.name()).thenReturn(parameterName);
    when(part.value()).thenReturn(content);
    return part;
  }

}