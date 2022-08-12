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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import io.github.gonalez.uptodatechecker.http.HttpClientImpl;
import io.github.gonalez.uptodatechecker.providers.ProvidersGetLatestVersionApiCollection;
import io.github.gonalez.uptodatechecker.providers.SpigetGetLatestVersionContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

/** Tests for {@link UpToDateChecker}. */
// TODO (gonalez): Improve tests
public class UpToDateCheckerTest {
  // ZNPCs resource id
  static final String RESOURCE_ID = "80940";

  @TempDir
  static Path temporaryDirectory;

  private static UpToDateChecker upToDateChecker;


  @BeforeAll
  static void setup() throws Exception {
    HttpClientImpl httpClient = new HttpClientImpl(directExecutor());
    upToDateChecker =
        new UpToDateCheckerImpl(
            directExecutor(),
            new FileUpdateDownloader(directExecutor(), httpClient, Options.DEFAULT_OPTIONS),
            GetLatestVersionApiProvider.of(ImmutableList.of(
                new ProvidersGetLatestVersionApiCollection(directExecutor(), httpClient))));
  }

  @Test
  public void testDownloading() throws Exception {
    GetLatestVersionContext latestVersionContext =
        SpigetGetLatestVersionContext.newBuilder()
            .setResourceId(RESOURCE_ID)
            .build();

    ListenableFuture<CheckUpToDateResponse> responseFuture =
        upToDateChecker.checkingUpToDateWithDownloadingAndScheduling()
            .requesting(
                CheckUpToDateRequest.newBuilder()
                    .setContext(latestVersionContext)
                    .setCurrentVersion("3.9")
                    .build())
            .then()
            .download(response -> UpdateDownloaderRequest.newBuilder()
                .setUrlToDownload(DownloadingUrls.SPIGET_DOWNLOAD_UPDATE_FILE_URL.apply(RESOURCE_ID))
                .setDownloadPath(temporaryDirectory, String.format("update-%s.jar", response.latestVersion()))
                .build())
            .response();

    // await result
    CheckUpToDateResponse response = responseFuture.get();

    assertTrue(response.isUpToDate());
    for (File file : temporaryDirectory.toFile().listFiles()) {
      assertTrue(file.getName().startsWith("update"));
    }
  }
}
