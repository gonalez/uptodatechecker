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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;
import io.github.gonalez.uptodatechecker.http.HttpClient;
import io.github.gonalez.uptodatechecker.http.HttpRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

/** A {@link UpdateDownloader} which can download update files to a path. */
@SuppressWarnings("UnstableApiUsage")
public class FileUpdateDownloader implements UpdateDownloader {
  private final Executor executor;
  private final HttpClient httpClient;
  private final Options options;
  
  public FileUpdateDownloader(
      Executor executor,
      HttpClient httpClient,
      Options options) {
    this.executor = checkNotNull(executor);
    this.httpClient = checkNotNull(httpClient);
    this.options = checkNotNull(options);
  }
  
  @Override
  public ListenableFuture<Boolean> downloadUpdate(UpdateDownloaderRequest request) {
    File file = new File(request.downloadPath());
    return LegacyFutures.transformAsync(
        httpClient.requestAsync(HttpRequest.of(request.urlToDownload(), options)),
        httpResponse -> {
          try (FileOutputStream outputStream = new FileOutputStream(file)) {
            ByteStreams.copy(new ByteArrayInputStream(httpResponse.body().getBytes()), outputStream);
          } catch (IOException e) {
            return Futures.immediateFailedFuture(e);
          }
          return Futures.immediateFuture(true);
        }, executor);
  }
}
