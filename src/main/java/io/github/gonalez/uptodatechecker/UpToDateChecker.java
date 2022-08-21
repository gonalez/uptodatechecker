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

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** The entry point of the UpToDateChecker library. */
public interface UpToDateChecker {
  /** @return a new builder to create an {@link UpToDateChecker}. */
  static UpToDateCheckerBuilder newBuilder() {
    return new UpToDateCheckerBuilder();
  }

  /**
   * Adds a {@link VersionProvider} into this up-to-date-checker. This will be used to determine the
   * {@link CheckUpToDateResponse#latestVersion()} of the given {@link CheckUpToDateOperation#requesting(
   * CheckUpToDateRequest) request}.
   *
   * <p>This should be called before performing any operations on this up-to-date-checker, i.e
   * ({@link #checkWithDownloadingAndScheduling()}).
   */
  <Context extends VersionProviderContext> ListenableFuture<Void> addVersionProvider(
      VersionProvider<Context> versionProvider);

  /**
   * The {@link CheckUpToDateOperation operation} on the fluent api that checks if a {@link
   * CheckUpToDateRequest} is up-to-date, it also provides other {@link DownloadingAndSchedulingOperation
   * operations} for scheduling and downloading the request.
   *
   * <pre>{@code
   * checkingUpToDateWithDownloadingAndScheduling()
   *     .requesting(checkUpToDateRequest)
   *     .then()
   *     .downloading(downloadRequest)
   *     .then()
   *     .schedule(period, timeUnit)
   *     .response();
   * }</pre>
   */
  CheckingUpToDateWithDownloadingAndScheduling checkWithDownloadingAndScheduling();

  /**
   * Functions to be called when we got the response for {@link
   * #checkWithDownloadingAndScheduling()}.
   */
  interface Callback {
    /** Called whenever a response has been completed with no errors. */
    default void onSuccess(CheckUpToDateResponse response) {}

    /** Called after {@link #onSuccess(CheckUpToDateResponse)} if the response is up-to-date. */
    default void onUpToDate(CheckUpToDateResponse response) {}

    /** Called after {@link #onSuccess(CheckUpToDateResponse)} if the response is not up-to-date. */
    default void onNotUpToDate(CheckUpToDateResponse response) {}

    /** Called if any error occurs. */
    default void onError(Throwable throwable) {}
  }

  /** Operation that allows a combination of two actions to be performed together. */
  interface ThenOperation<T> {
    /** Switch to the next operation. */
    T then();
  }

  /**
   * The base operation of the api required for the basic functionality (checking for up-to-date an
   * url).
   */
  interface CheckUpToDateOperation<T> {
    T requesting(CheckUpToDateRequest checkUpToDateRequest);
  }

  /** Operation that adds support for downloading a new update for the up-to-date-checker. */
  interface DownloadingOperation<T> {
    T download(
        Function<CheckUpToDateResponse, UpdateDownloaderRequest>
            computeUpdateDownloaderRequestFunction);
  }

  /** Operation that adds support for scheduling the up-to-date-checker. */
  interface SchedulingOperation<T> {
    T schedule(long period, TimeUnit unit);
  }

  /** Operation that is both a {@link DownloadingOperation} and a {@link SchedulingOperation}. */
  interface DownloadingAndSchedulingOperation<T>
      extends DownloadingOperation<T>, SchedulingOperation<T> {}

  /** Represents the composed response from the operations that were called. */
  interface ResponseOperation {
    ListenableFuture<CheckUpToDateResponse> response();
  }

  /**
   * @see #checkWithDownloadingAndScheduling()
   */
  interface CheckingUpToDateWithDownloadingAndScheduling
      extends CheckUpToDateOperation<CheckingUpToDateWithDownloadingAndScheduling>,
          ThenOperation<
              DownloadingAndSchedulingOperation<CheckingUpToDateWithDownloadingAndScheduling>>,
          ResponseOperation {}
}
