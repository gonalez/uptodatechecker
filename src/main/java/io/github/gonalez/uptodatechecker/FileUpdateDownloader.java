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
import com.google.common.util.concurrent.ListeningExecutorService;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

/** A {@link UpdateDownloader} which can download update files to a path. */
@SuppressWarnings("UnstableApiUsage")
public class FileUpdateDownloader implements UpdateDownloader {
  private final ListeningExecutorService executor;
  private final UrlBytesReader urlBytesReader;
  
  public FileUpdateDownloader(
      ListeningExecutorService executor,
      UrlBytesReader urlBytesReader) {
    this.executor = checkNotNull(executor);
    this.urlBytesReader = checkNotNull(urlBytesReader);
  }
  
  @Override
  public ListenableFuture<Boolean> downloadUpdate(UpdateDownloaderRequest request) {
    return Futures.catchingAsync(
        executor.submit(() -> {
          byte[] readUrlBytes = UpToDateCheckerHelper.urlContentToBytes(urlBytesReader, request.urlToDownload());
          try (FileOutputStream outputStream = new FileOutputStream(request.downloadPath())) {
            ByteStreams.copy(new ByteArrayInputStream(readUrlBytes), outputStream);
          }
          return true;
        }),
        Exception.class,
        cause -> Futures.immediateFuture(false));
  }
}