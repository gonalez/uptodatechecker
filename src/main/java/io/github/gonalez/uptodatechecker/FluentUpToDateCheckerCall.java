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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/** Provides a fluent builder API for making {@link UpToDateChecker} calls. */
public interface FluentUpToDateCheckerCall {
  /** Creates a new {@link FluentUpToDateCheckerCall}. */
  static FluentUpToDateCheckerCall newCall(CheckUpToDateRequest request) {
    return new FluentUpToDateCheckerCallImpl(request);
  }
  
  /** Set if we should call shutdown the executor service when cancelling {@link #start()}. */
  FluentUpToDateCheckerCall setShutdownOnCancel(boolean shutdownOnCancel);
  
  /** Set the executor which will manage the up-to-date checker. */
  FluentUpToDateCheckerCall withExecutorService(ExecutorService executorService);
  
  /** Set the callback to be executed when the up-to-date checker call has been completed. */
  FluentUpToDateCheckerCall withCallback(UpToDateChecker.Callback callback);
  
  /** Set the optional callback to be also executed when the up-to-date checker call has been completed. */
  FluentUpToDateCheckerCall withOptionalCallback(UpToDateChecker.Callback callback);
  
  /** Sets the UrlBytesReader that's used to read the content of the request url. */
  FluentUpToDateCheckerCall withUrlBytesReader(UrlBytesReader urlBytesReader);
  
  /** Sets the strategy to use to determine if the version of the request matches with the response. */
  FluentUpToDateCheckerCall withMatchingStrategy(BiFunction<String, String, Boolean> matchStrategy);
  
  /** Establishes that the call must repeat every {@code period} defined by the {@code timeUnit}. */
  FluentUpToDateCheckerCall scheduling(long period, TimeUnit timeUnit);
  
  /**
   * Set the request to be used to download a new update when the call response is not
   * {@link UpToDateChecker.Callback#onNotUpToDate(CheckUpToDateResponse) not up-to-date}
   */
  FluentUpToDateCheckerCall setDownloadRequest(UpdateDownloaderRequest updateDownloaderRequest);
  
  /**
   * Starts the up-to-date check call.
   *
   * <p>Returns a {@link Cancellable} representing the current call, by calling this it cancels any
   * active resources created by {@code this} call.
   */
  Cancellable start();
}
