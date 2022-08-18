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

import com.google.common.util.concurrent.ListenableFuture;
import io.github.gonalez.uptodatechecker.http.HttpClientImpl;
import io.github.gonalez.uptodatechecker.providers.SpigetGetLatestVersionApi;
import io.github.gonalez.uptodatechecker.providers.SpigetGetLatestVersionContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/** Tests for {@link UpToDateChecker}. */
public class UpToDateCheckerTest {
  // ZNPCs resource id
  public static final String RESOURCE_ID = "80940";

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  private static final BiFunction<String, String, Boolean> EQUAL_STRATEGY = String::equals;

  @TempDir private static Path temporaryDirectory;

  private static UpToDateChecker upToDateChecker;

  private static CheckUpToDateRequest checkUpToDateRequest;

  @BeforeAll
  static void setup() {
    HttpClientImpl httpClient = new HttpClientImpl(EXECUTOR_SERVICE);
    upToDateChecker =
        new UpToDateCheckerImpl(
            EXECUTOR_SERVICE,
            Optional.of(
                new FileUpdateDownloader(EXECUTOR_SERVICE, httpClient, Options.DEFAULT_OPTIONS)),
            EQUAL_STRATEGY);
    upToDateChecker.addLatestVersionApi(new SpigetGetLatestVersionApi(EXECUTOR_SERVICE, httpClient));

    checkUpToDateRequest =
        CheckUpToDateRequest.newBuilder()
            .setContext(
                SpigetGetLatestVersionContext.newBuilder().setResourceId(RESOURCE_ID).build())
            .setCurrentVersion("3.9")
            .build();
  }

  @Test
  public void testScheduling() throws Exception {
    AtomicInteger atomicInteger = new AtomicInteger();

    CheckUpToDateRequest scheduleRequest =
        CheckUpToDateRequest.newBuilder()
            .setContext(checkUpToDateRequest.context())
            .setCurrentVersion(checkUpToDateRequest.currentVersion())
            .setOptionalCallback(
                Optional.of(
                    new UpToDateChecker.Callback() {
                      @Override
                      public void onSuccess(CheckUpToDateResponse response) {
                        atomicInteger.incrementAndGet();
                      }
                    }))
            .build();

    ListenableFuture<CheckUpToDateResponse> responseFuture =
        upToDateChecker
            .checkingUpToDateWithDownloadingAndScheduling()
            .requesting(scheduleRequest)
            .then()
            .schedule(1, TimeUnit.SECONDS)
            .response();

    Thread.sleep(5000);
    responseFuture.cancel(true);

    assertThat(atomicInteger.get()).isEqualTo(5);
  }

  @Test
  public void testDownloading() throws Exception {
    ListenableFuture<CheckUpToDateResponse> responseFuture =
        upToDateChecker
            .checkingUpToDateWithDownloadingAndScheduling()
            .requesting(checkUpToDateRequest)
            .then()
            .download(
                response ->
                    UpdateDownloaderRequest.newBuilder()
                        .setUrlToDownload(
                            DownloadingUrls.SPIGET_DOWNLOAD_UPDATE_FILE_URL.apply(RESOURCE_ID))
                        .setDownloadPath(
                            temporaryDirectory,
                            String.format("update-%s.jar", response.latestVersion()))
                        .build())
            .response();

    // await result
    CheckUpToDateResponse response = responseFuture.get();
    assertThat(response.isUpToDate()).isTrue();

    File[] tempFiles = temporaryDirectory.toFile().listFiles();
    assertThat(tempFiles).isNotNull();
    assertThat(tempFiles).hasLength(1);

    File downloadedFile = tempFiles[0];
    assertThat(downloadedFile.getName()).startsWith("update");
  }
}
