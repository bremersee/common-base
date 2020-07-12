package org.bremersee.web.multipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileAwareMultipartFile implements MultipartFile {

  private final File file;

  private final String parameterName;

  private final String originalFilename;

  private final String contentType;

  private FileAwareMultipartFile() {
    this.file = null;
    this.parameterName = null;
    this.originalFilename = null;
    this.contentType = null;
  }

  public FileAwareMultipartFile(MultipartFile multipartFile) throws IOException {
    this(multipartFile, (File) null);
  }

  public FileAwareMultipartFile(MultipartFile multipartFile, String tmpDir) throws IOException {
    this(multipartFile, getTmpDir(tmpDir));
  }

  public FileAwareMultipartFile(MultipartFile multipartFile, File tmpDir) throws IOException {
    if (multipartFile == null) {
      this.file = null;
      this.parameterName = null;
      this.originalFilename = null;
      this.contentType = null;
    } else {
      if (multipartFile.isEmpty()) {
        this.file = null;
      } else {
        if (multipartFile instanceof FileAwareMultipartFile) {
          this.file = ((FileAwareMultipartFile) multipartFile).file;
        } else {
          this.file = getTmpFile(getTmpDir(tmpDir));
          FileCopyUtils.copy(
              multipartFile.getInputStream(),
              Files.newOutputStream(
                  this.file.toPath(),
                  StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
        }
      }
      this.parameterName = multipartFile.getName();
      this.originalFilename = multipartFile.getOriginalFilename();
      this.contentType = multipartFile.getContentType();
    }
  }

  public FileAwareMultipartFile(
      InputStream inputStream,
      String parameterName,
      String originalFilename,
      String contentType) throws IOException {
    this(inputStream, (File) null, parameterName, originalFilename, contentType);
  }

  public FileAwareMultipartFile(
      InputStream inputStream,
      String tmpDir,
      String parameterName,
      String originalFilename,
      String contentType) throws IOException {
    this(inputStream, getTmpDir(tmpDir), parameterName, originalFilename, contentType);
  }

  public FileAwareMultipartFile(
      InputStream inputStream,
      File tmpDir,
      String parameterName,
      String originalFilename,
      String contentType) throws IOException {
    if (inputStream != null) {
      this.file = getTmpFile(tmpDir);
      FileCopyUtils.copy(
          inputStream,
          Files.newOutputStream(
              this.file.toPath(),
              StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
    } else {
      this.file = null;
    }
    this.parameterName = parameterName;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
  }

  public FileAwareMultipartFile(
      Path file,
      String parameterName,
      String originalFilename,
      String contentType) {
    this.file = file != null ? file.toFile() : null;
    this.parameterName = parameterName;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
  }

  public FileAwareMultipartFile(
      File file,
      String parameterName,
      String originalFilename,
      String contentType) {
    this.file = file;
    this.parameterName = parameterName;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
  }

  public static FileAwareMultipartFile empty() {
    return new FileAwareMultipartFile();
  }

  public static void delete(MultipartFile multipartFile) {
    if (multipartFile != null && multipartFile.getResource().isFile()) {
      try {
        Files.delete(multipartFile.getResource().getFile().toPath());
      } catch (Exception ignored) {
        // ignored
      }
    }
  }

  private static File getTmpDir(String tmpDir) {
    return getTmpDir(StringUtils.hasText(tmpDir) ? new File(tmpDir) : null);
  }

  private static File getTmpDir(File tmpDir) {
    return Optional.ofNullable(tmpDir)
        .filter(dir -> dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite())
        .orElseGet(() -> new File(System.getProperty("java.io.tmpdir")));
  }

  private static File getTmpFile(File tmpDir) throws IOException {
    return File.createTempFile("uploaded-", ".tmp", tmpDir);
  }

  @NonNull
  @Override
  public String getName() {
    return parameterName == null ? "" : parameterName;
  }

  @Override
  public String getOriginalFilename() {
    return originalFilename;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return getSize() == 0;
  }

  @Override
  public long getSize() {
    return isFileValid() ? file.length() : 0;
  }

  @NonNull
  @Override
  public byte[] getBytes() throws IOException {
    return isEmpty() ? new byte[0] : FileCopyUtils.copyToByteArray(new FileInputStream(file));
  }

  @NonNull
  @Override
  public InputStream getInputStream() throws IOException {
    return isEmpty() ? new ByteArrayInputStream(new byte[0]) : new FileInputStream(file);
  }

  @NonNull
  @Override
  public Resource getResource() {
    return isEmpty() ? new EmptyResource() : new FileSystemResource(file);
  }

  @Override
  public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
    if (isFileValid()) {
      FileCopyUtils.copy(file, dest);
    }
  }

  private boolean isFileValid() {
    return file != null && file.exists() && file.isFile() && file.canRead();
  }

  @SuppressWarnings("Lombok")
  @EqualsAndHashCode
  private static class EmptyResource extends AbstractResource {

    private EmptyResource() {
    }

    @NonNull
    @Override
    public String getDescription() {
      return "Empty resource.";
    }

    @NonNull
    @Override
    public InputStream getInputStream() {
      return new ByteArrayInputStream(new byte[0]);
    }
  }

}
