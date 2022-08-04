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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/** Default implementation for {@link FluentUpToDateCheckerCall}. */
public class FluentUpToDateCheckerCallImpl implements FluentUpToDateCheckerCall {
  private boolean shutdownOnCancel;
  private ExecutorService executorService;
  private UpToDateChecker.Callback callback, checkerCallback;
  private UrlBytesReader urlBytesReader = UrlBytesReader.defaultInstance();
  private BiFunction<String, String, Boolean> matchStrategy = UpToDateCheckerHelper.EQUAL_STRATEGY;
  
  private long period;
  private TimeUnit timeUnit;
  
  private UpdateDownloaderRequest updateDownloaderRequest;
  
  private final CheckUpToDateRequest request;
  
  public FluentUpToDateCheckerCallImpl(CheckUpToDateRequest request) {
    this.request = checkNotNull(request);
  }
  
  @Override
  public FluentUpToDateCheckerCall setShutdownOnCancel(boolean shutdownOnCancel) {
    this.shutdownOnCancel = shutdownOnCancel;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall withExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall withCallback(UpToDateChecker.Callback callback) {
    this.callback = callback;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall withUrlBytesReader(UrlBytesReader urlBytesReader) {
    this.urlBytesReader = urlBytesReader;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall withMatchingStrategy(BiFunction<String, String, Boolean> matchStrategy) {
    this.matchStrategy = matchStrategy;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall withOptionalCallback(UpToDateChecker.Callback callback) {
    this.checkerCallback = callback;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall scheduling(long period, TimeUnit timeUnit) {
    this.period = period;
    this.timeUnit = timeUnit;
    return this;
  }
  
  @Override
  public FluentUpToDateCheckerCall setDownloadRequest(UpdateDownloaderRequest updateDownloaderRequest) {
    this.updateDownloaderRequest = updateDownloaderRequest;
    return this;
  }
  
  @Override
  public Cancellable start() {
    checkNotNull(request);
    checkNotNull(urlBytesReader);
    checkNotNull(matchStrategy);
    if (executorService == null) {
      executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }
    UpToDateChecker upToDateChecker =
        new UpToDateCheckerImpl(
            executorService,
            urlBytesReader,
            matchStrategy,
            Optional.ofNullable(checkerCallback));
    ImmutableList.Builder<Cancellable> cancellableBuilder = ImmutableList.builder();
    ImmutableList.Builder<UpToDateChecker.Callback> callbackBuilder = ImmutableList.builder();
    if (updateDownloaderRequest != null) {
      UpdateDownloader updateDownloader = new FileUpdateDownloader(executorService, urlBytesReader);
      callbackBuilder.add(new UpToDateChecker.Callback() {
        @Override
        public void onNotUpToDate(CheckUpToDateResponse response) {
          updateDownloader.downloadUpdate(
              UpdateDownloaderRequest.newBuilder()
                  .setDownloadPath(updateDownloaderRequest.downloadPath())
                  .setUrlToDownload(updateDownloaderRequest.urlToDownload())
                  .build());
        }
      });
    }
    if (callback != null) {
      callbackBuilder.add(callback);
    }
    UpToDateChecker.Callback callback1 = UpToDateChecker.Callback.chaining(callbackBuilder.build());
    if (timeUnit != null) {
      ListenableFuture<?> scheduleUpToDateCheckerTask =
          LegacyFutures.schedulePeriodicAsync(
                  () -> {
                    // Reset UpToDateChecker state
                    upToDateChecker.clear();
                    return upToDateChecker.checkUpToDate(request, callback1);
                  },
                  period,
                  timeUnit, executorService);
      cancellableBuilder.add(() -> scheduleUpToDateCheckerTask.cancel(true));
    } else {
      upToDateChecker.checkUpToDate(request, callback1);
    }
    cancellableBuilder.add(upToDateChecker::clear);
    if (shutdownOnCancel) {
      cancellableBuilder.add(executorService::shutdownNow);
    }
    return Cancellable.chaining(cancellableBuilder.build());
  }
}
