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

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gonalez.uptodatechecker.http.HttpClientImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

/** Tests for {@link UpdateDownloader}. */
public class UpdateDownloaderTest {
  private static final String EXAMPLE_DOWNLOAD_URL = "https://www.gstatic.com/icing/idd/apitest/zip_test_folder.zip";

  @TempDir
  static Path temporaryDirectory;
  
  static FileUpdateDownloader updateDownloader;
  
  @BeforeAll
  static void setup() throws Exception {
    updateDownloader =
        new FileUpdateDownloader(
            directExecutor(), new HttpClientImpl(directExecutor()),
            Options.newBuilder().build());
  }
  
  @Test
  public void testFileUpdateDownloader() throws Exception {
    updateDownloader.downloadUpdate(
            UpdateDownloaderRequest.newBuilder()
                .setUrlToDownload(EXAMPLE_DOWNLOAD_URL)
                .setDownloadPath(temporaryDirectory, "example.zip")
                .build())
        .get();
    for (File file : temporaryDirectory.toFile().listFiles()) {
      assertEquals("example.zip", file.getName());
    }
  }
}
