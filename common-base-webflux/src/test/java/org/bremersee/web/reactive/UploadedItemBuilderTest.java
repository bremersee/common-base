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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bremersee.exception.ServiceException;
import org.bremersee.web.ReqParam;
import org.bremersee.web.UploadedItem;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The reactive uploaded item builder test.
 *
 * @author Christian Bremer
 */
class UploadedItemBuilderTest {

  /**
   * Build with null.
   */
  @Test
  void buildWithNull() {
    UploadedItemBuilder builder = new UploadedItemBuilderImpl();
    StepVerifier
        .create(builder.build(null))
        .assertNext(uploadedItem -> {
          assertTrue(uploadedItem.isEmpty());
          assertNull(uploadedItem.getContentType());
          assertNull(uploadedItem.getFilename());
          assertNull(uploadedItem.getItem());
          assertEquals(0L, uploadedItem.getLength());
        })
        .verifyComplete();
  }

  /**
   * Build with file part.
   */
  @Test
  void buildWithFilePart() {
    UploadedItemBuilder builder = new UploadedItemBuilderImpl(
        System.getProperty("java.io.tmpdir"));
    final byte[] value = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
    StepVerifier
        .create(builder.build(createFilePart(value, MediaType.TEXT_PLAIN, "test.txt")))
        .assertNext(uploadedItem -> {
          try {
            assertFalse(uploadedItem.isEmpty());
            assertNotNull(uploadedItem.getItem());
            assertEquals(MediaType.TEXT_PLAIN_VALUE, uploadedItem.getContentType());
            assertEquals(value.length, (int) uploadedItem.getLength());
            assertEquals("test.txt", uploadedItem.getFilename());
            assertTrue(uploadedItem.getItem() instanceof Path);
            assertArrayEquals(value, Files.readAllBytes((Path) uploadedItem.getItem()));

          } catch (IOException e) {
            throw ServiceException.internalServerError("Fatal error", e);
          } finally {
            uploadedItem.delete();
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
    UploadedItemBuilder builder = new UploadedItemBuilderImpl(
        new File(System.getProperty("java.io.tmpdir")));
    final byte[] value = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(value)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("image/png")
        .build();
    StepVerifier
        .create(builder.build(createFormFieldPart(new DataUrlSerializer().serialize(dataUrl))))
        .assertNext(uploadedItem -> {
          try {
            assertFalse(uploadedItem.isEmpty());
            assertNotNull(uploadedItem.getItem());
            assertEquals(MediaType.IMAGE_PNG_VALUE, uploadedItem.getContentType());
            assertEquals(value.length, (int) uploadedItem.getLength());
            assertNull(uploadedItem.getFilename());
            assertTrue(uploadedItem.getItem() instanceof byte[]);
            assertArrayEquals(value, (byte[]) uploadedItem.getItem());

          } finally {
            uploadedItem.delete();
          }
        })
        .verifyComplete();
  }

  /**
   * Build from first parameter value.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildFromFirstParameterValue() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.set("part1", createFilePart(content0, MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.set("part2", createFormFieldPart(new DataUrlSerializer().serialize(dataUrl)));

    UploadedItemBuilder builder = new UploadedItemBuilderImpl();
    StepVerifier.create(builder
        .buildFromFirstParameterValue(multiPartData, new ReqParam("part1"), new ReqParam("part2")))
        .assertNext(uploadedItems -> {
          try {
            assertEquals(2, uploadedItems.size());

            UploadedItem<?> obj0 = UploadedItemBuilder.getUploadedItem(uploadedItems, 0);
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getLength());
            assertTrue(obj0.getItem() instanceof Path);
            assertArrayEquals(content0, Files.readAllBytes((Path) obj0.getItem()));

            UploadedItem<?> obj1 = UploadedItemBuilder.getUploadedItem(uploadedItems, 1);
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getLength());
            assertTrue(obj1.getItem() instanceof byte[]);
            assertArrayEquals(content1, (byte[]) obj1.getItem());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            uploadedItems.forEach(UploadedItem::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build from all parameter values.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildFromAllParameterValues() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.add("part", createFilePart(content0, MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.add("part", createFormFieldPart(new DataUrlSerializer().serialize(dataUrl)));

    UploadedItemBuilder builder = new UploadedItemBuilderImpl();
    StepVerifier
        .create(builder.buildFromAllParameterValues(multiPartData, new ReqParam("part")))
        .assertNext(uploadedItems -> {
          try {
            assertEquals(2, uploadedItems.size());

            UploadedItem<?> obj0 = UploadedItemBuilder.getUploadedItem(uploadedItems, 0);
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getLength());
            assertTrue(obj0.getItem() instanceof Path);
            assertArrayEquals(content0, Files.readAllBytes((Path) obj0.getItem()));

            UploadedItem<?> obj1 = UploadedItemBuilder.getUploadedItem(uploadedItems, 1);
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getLength());
            assertTrue(obj1.getItem() instanceof byte[]);
            assertArrayEquals(content1, (byte[]) obj1.getItem());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            uploadedItems.forEach(UploadedItem::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build map from first parameter value.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildMapFromFirstParameterValue() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.set("part1", createFilePart(content0, MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.set("part2", createFormFieldPart(new DataUrlSerializer().serialize(dataUrl)));

    UploadedItemBuilder builder = new UploadedItemBuilderImpl();
    StepVerifier.create(builder
        .buildMapFromFirstParameterValue(multiPartData, new ReqParam("part1"),
            new ReqParam("part2")))
        .assertNext(uploadedItems -> {
          try {
            assertEquals(2, uploadedItems.size());

            UploadedItem<?> obj0 = UploadedItemBuilder.getUploadedItem(uploadedItems, "part1");
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getLength());
            assertTrue(obj0.getItem() instanceof Path);
            assertArrayEquals(content0, Files.readAllBytes((Path) obj0.getItem()));

            UploadedItem<?> obj1 = UploadedItemBuilder.getUploadedItem(uploadedItems, "part2");
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getLength());
            assertTrue(obj1.getItem() instanceof byte[]);
            assertArrayEquals(content1, (byte[]) obj1.getItem());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            uploadedItems.values().forEach(UploadedItem::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Build map from all parameter values.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildMapFromAllParameterValues() throws IOException {
    MultiValueMap<String, Part> multiPartData = new LinkedMultiValueMap<>();
    final byte[] content0 = "image-content".getBytes(StandardCharsets.UTF_8);
    multiPartData.add("part", createFilePart(content0, MediaType.IMAGE_JPEG, "img.jpg"));
    final byte[] content1 = "text".getBytes(StandardCharsets.UTF_8);
    DataUrl dataUrl = new DataUrlBuilder()
        .setData(content1)
        .setCharset(StandardCharsets.UTF_8.name())
        .setEncoding(DataUrlEncoding.BASE64)
        .setMimeType("text/plain")
        .build();
    multiPartData.add("part", createFormFieldPart(new DataUrlSerializer().serialize(dataUrl)));

    UploadedItemBuilder builder = new UploadedItemBuilderImpl();
    StepVerifier
        .create(builder.buildMapFromAllParameterValues(multiPartData, new ReqParam("part")))
        .assertNext(map -> {
          List<UploadedItem<?>> uploadedItems = map.get("part");
          assertNotNull(uploadedItems);
          try {
            assertEquals(2, uploadedItems.size());

            UploadedItem<?> obj0 = UploadedItemBuilder.getUploadedItem(uploadedItems, 0);
            assertNotNull(obj0);
            assertEquals(MediaType.IMAGE_JPEG_VALUE, obj0.getContentType());
            assertEquals(content0.length, (int) obj0.getLength());
            assertTrue(obj0.getItem() instanceof Path);
            assertArrayEquals(content0, Files.readAllBytes((Path) obj0.getItem()));

            UploadedItem<?> obj1 = UploadedItemBuilder.getUploadedItem(uploadedItems, 1);
            assertNotNull(obj1);
            assertEquals(MediaType.TEXT_PLAIN_VALUE, obj1.getContentType());
            assertEquals(content1.length, (int) obj1.getLength());
            assertTrue(obj1.getItem() instanceof byte[]);
            assertArrayEquals(content1, (byte[]) obj1.getItem());

          } catch (IOException e) {
            throw ServiceException.internalServerError("Internal error", e);
          } finally {
            uploadedItems.forEach(UploadedItem::delete);
          }
        })
        .verifyComplete();
  }

  /**
   * Gets uploaded item from list that does not exist.
   */
  @Test
  void getUploadedItemFromListThatDoesNotExist() {
    assertTrue(UploadedItemBuilder.getUploadedItem(Collections.emptyList(), 1).isEmpty());
  }

  /**
   * Gets uploaded item from map that does not exist.
   */
  @Test
  void getUploadedItemFromMapThatDoesNotExist() {
    assertTrue(UploadedItemBuilder.getUploadedItem(Collections.emptyMap(), "foo").isEmpty());
  }

  /**
   * Default builder.
   */
  @Test
  void defaultBuilder() {
    assertNotNull(UploadedItemBuilder.defaultBuilder());
  }

  /**
   * Default builder from string.
   */
  @Test
  void defaultBuilderFromString() {
    assertNotNull(UploadedItemBuilder.defaultBuilder(System.getProperty("java.io.tmpdir")));
  }

  /**
   * Default builder from file.
   */
  @Test
  void defaultBuilderFromFile() {
    assertNotNull(UploadedItemBuilder
        .defaultBuilder(new File(System.getProperty("java.io.tmpdir"))));
  }

  /**
   * Default builder from path.
   */
  @Test
  void defaultBuilderFromPath() {
    assertNotNull(UploadedItemBuilder
        .defaultBuilder(Paths.get("java.io.tmpdir")));
  }

  private FilePart createFilePart(byte[] content, MediaType contentType, String filename) {
    FilePart part = mock(FilePart.class);
    when(part.filename()).thenReturn(filename);
    when(part.transferTo(any(File.class))).then(invocationOnMock -> {
      try {
        File file = invocationOnMock.getArgument(0);
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

  private FormFieldPart createFormFieldPart(String content) {
    FormFieldPart part = mock(FormFieldPart.class);
    when(part.value()).thenReturn(content);
    return part;
  }

}