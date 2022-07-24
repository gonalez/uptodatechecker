/*
 * Copyright 2022 - Gaston Gonzalez (Gonalez)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.gonalez.uptodatechecker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

/** Tests for {@link UpdateDownloader}. */
public class UpdateDownloaderTest {
  private static final String DOWNLOAD_FILE_URL = "https://www.gstatic.com/suggest-dev/odws1_empty.jar";
  
  @TempDir
  static Path temporaryDirectory;
  
  static FileUpdateDownloader updateDownloader;
  
  @BeforeAll
  static void setup() throws Exception {
    updateDownloader =
        new FileUpdateDownloader(
            MoreExecutors.newDirectExecutorService(),
            UrlBytesReader.defaultInstance());
  }
  
  @Test
  public void testFileUpdateDownloader() throws Exception {
    updateDownloader.downloadUpdate(
        UpdateDownloaderRequest.newBuilder()
            .setUrlToDownload(DOWNLOAD_FILE_URL)
            .setDownloadPath(temporaryDirectory, "test.jar")
            .build())
        .get();
    for (File file : temporaryDirectory.toFile().listFiles()) {
      assertEquals("test.jar", file.getName());
    }
  }
}