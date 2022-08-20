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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.util.concurrent.MoreExecutors;
import io.github.gonalez.uptodatechecker.http.HttpClientImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;

/** Tests for {@link UpdateDownloader}. */
public class UpdateDownloaderTest {
  private static final String EXAMPLE_DOWNLOAD_URL =
      "https://www.gstatic.com/icing/idd/apitest/zip_test_folder.zip";

  private static final Executor EXECUTOR = MoreExecutors.directExecutor();

  @TempDir private static Path temporaryDirectory;

  private static UpdateDownloader updateDownloader;

  @BeforeAll
  static void setup() throws Exception {
    updateDownloader =
        new FileUpdateDownloader(EXECUTOR, new HttpClientImpl(EXECUTOR),
            Options.DEFAULT_OPTIONS);
  }

  @Test
  public void testFileUpdateDownloader() throws Exception {
    updateDownloader
        .downloadUpdate(
            UpdateDownloaderRequest.newBuilder()
                .setUrlToDownload(EXAMPLE_DOWNLOAD_URL)
                .setDownloadPath(temporaryDirectory, "example.zip")
                .build())
        .get();

    File[] tempFiles = temporaryDirectory.toFile().listFiles();
    assertThat(tempFiles).isNotNull();
    assertThat(tempFiles).hasLength(1);

    File downloadedFile = tempFiles[0];
    assertThat(downloadedFile.getName()).isEqualTo("example.zip");
  }
}
