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

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** The entry point of the UpToDateChecker library. */
public interface UpToDateChecker {
  /** Functions to be called when we got the response for {@link #checkingUpToDateWithDownloadingAndScheduling()}. */
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

  /**
   * Returns the default implementation for the fluent api of the UpToDateChecker library that allows
   * checking for up-to-date an url, scheduling and downloading against the checker.
   *
   * <p>Downloading and scheduling are available after calling {@link ThenOperation#then()} if the
   * {@link CheckUpToDateOperation} was configured correctly, if it was not configured correctly it
   * will not be possible to perform the next operations.
   */
  CheckingUpToDateWithDownloadingAndScheduling checkingUpToDateWithDownloadingAndScheduling();

  /** Operation that allows a combination of two actions to be performed together. */
  interface ThenOperation<T> {
    /** Switch to the next operation. */
    T then();
  }

  /** The base operation of the api required for the basic functionality (checking for up-to-date an url). */
  interface CheckUpToDateOperation<T> {
    T withRequest(String currentVersion, GetLatestVersionContext latestVersionContext);
    T withCallback(@Nullable Callback callback);
  }

  /** Operation that adds support for downloading a new update for the up-to-date-checker. */
  interface DownloadingOperation<T> {
    T download(Function<CheckUpToDateResponse, UpdateDownloaderRequest> computeUpdateDownloaderRequestFunction);
  }

  /** Operation that adds support for scheduling the up-to-date-checker. */
  interface SchedulingOperation<T> {
    T schedule(long period, TimeUnit unit);
  }

  /** Chains {@link DownloadingOperation} and {@link SchedulingOperation} together. */
  interface DownloadingAndSchedulingOperation<T> {
    DownloadingOperation<T> downloading();
    SchedulingOperation<T> scheduling();
  }

  /**
   * The last operation that the user wants to execute, after this is called is not possible to reach other operations.
   */
  interface GetOperation {
    ListenableFuture<CheckUpToDateResponse> response();
  }

  /** @see #checkingUpToDateWithDownloadingAndScheduling() */
  interface CheckingUpToDateWithDownloadingAndScheduling
      extends CheckUpToDateOperation<CheckingUpToDateWithDownloadingAndScheduling>, ThenOperation<
         DownloadingAndSchedulingOperation<CheckingUpToDateWithDownloadingAndScheduling>>, GetOperation {}
}
