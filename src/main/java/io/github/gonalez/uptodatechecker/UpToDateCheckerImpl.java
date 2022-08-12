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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A basic, default implementation for {@link UpToDateChecker}.
 *
 * <p>To determine if something is up-to-date or not, we apply the {@code versionMatchStrategy} function to the
 * given request {@link CheckUpToDateRequest#currentVersion() current version} and the latest version of the request
 * determined by the {@link CheckUpToDateRequest#context()}}.
 *
 * <p>{@code latestVersionApiProviderSupplier} supplies the instance of the {@link GetLatestVersionApiProvider} for
 * getting the appropriate {@link GetLatestVersionApi} of the given {@link CheckUpToDateRequest#context() request context}.
 */
@SuppressWarnings("UnstableApiUsage")
@NotThreadSafe
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final Executor executor;
  private final UpdateDownloader updateDownloader;
  private final GetLatestVersionApiProvider latestVersionApiProvider;

  public UpToDateCheckerImpl(
      Executor executor,
      UpdateDownloader updateDownloader,
      GetLatestVersionApiProvider latestVersionApiProvider) {
    this.executor = checkNotNull(executor);
    this.updateDownloader = checkNotNull(updateDownloader);
    this.latestVersionApiProvider = checkNotNull(latestVersionApiProvider);

  }

  @Override
  public CheckingUpToDateWithDownloadingAndScheduling checkingUpToDateWithDownloadingAndScheduling() {
    return new CheckingUpToDateWithDownloadingAndSchedulingImpl();
  }

  /** Base implementation for {@link CheckingUpToDateWithDownloadingAndScheduling}. */
  private class CheckingUpToDateWithDownloadingAndSchedulingImpl implements CheckingUpToDateWithDownloadingAndScheduling {
    private final CheckUpToDateRequest.Builder requestBuilder = CheckUpToDateRequest.newBuilder();

    private final List<Function<ListenableFuture<CheckUpToDateResponse>,
        ListenableFuture<CheckUpToDateResponse>>> operations = new ArrayList<>();

    /** @return {@code this}. */
    CheckingUpToDateWithDownloadingAndScheduling thisInstance() {
      return this;
    }

    @Override
    public CheckingUpToDateWithDownloadingAndScheduling requesting(CheckUpToDateRequest checkUpToDateRequest) {
      requestBuilder.setCurrentVersion(checkUpToDateRequest.currentVersion());
      requestBuilder.setContext(checkUpToDateRequest.context());
      requestBuilder.setOptionalCallback(checkUpToDateRequest.optionalCallback());
      return thisInstance();
    }

    @Override
    public DownloadingAndSchedulingOperation<CheckingUpToDateWithDownloadingAndScheduling> then() {
      return new DownloadingAndSchedulingOperation<>() {
        @Override
        public CheckingUpToDateWithDownloadingAndScheduling schedule(long period, TimeUnit unit) {
          operations.add(checkUpToDateResponseListenableFuture ->
              LegacyFutures.schedulePeriodicAsync(
                  () -> checkUpToDate(requestBuilder.build(), executor),
                  period,
                  unit,
                  executor));
          return thisInstance();
        }

        @Override
        public CheckingUpToDateWithDownloadingAndScheduling download(Function<CheckUpToDateResponse, UpdateDownloaderRequest> computeUpdateDownloaderRequestFunction) {
          operations.add(future ->
              LegacyFutures.transformAsync(
                  future,
                  response -> LegacyFutures.transformAsync(
                      updateDownloader.downloadUpdate(computeUpdateDownloaderRequestFunction.apply(response)),
                      unused -> {
                        return Futures.immediateFuture(response);
                      }, executor), executor));
          return thisInstance();
        }
      };
    }

    @Override
    public ListenableFuture<CheckUpToDateResponse> response() {
      ListenableFuture<CheckUpToDateResponse> responseListenableFuture =
          checkUpToDate(requestBuilder.build(), executor);
      for (Function<ListenableFuture<CheckUpToDateResponse>,
          ListenableFuture<CheckUpToDateResponse>> operation : operations) {
        responseListenableFuture = operation.apply(responseListenableFuture);
      }
      return responseListenableFuture;
    }

    private ListenableFuture<CheckUpToDateResponse> checkUpToDate(
        CheckUpToDateRequest request, Executor executor) {
      Optional<GetLatestVersionApi<GetLatestVersionContext>> maybeGetProviderForContext =
          latestVersionApiProvider.get(request.context());
      if (maybeGetProviderForContext.isEmpty()) {
        return Futures.immediateFailedFuture(
            UpToDateCheckerException.newBuilder()
                .setExceptionCode(UpToDateCheckerExceptionCode.FAIL_TO_GET_LATEST_VERSION_FROM_CONTEXT)
                .build());
      }
      Optional<Callback> optionalCallback = request.optionalCallback();
      return LegacyFutures.catchingAsync(
          LegacyFutures.transformAsync(
              maybeGetProviderForContext.get().getLatestVersion(request.context()),
              latestVersion -> {
                CheckUpToDateResponse response =
                    CheckUpToDateResponse.newBuilder()
                        .setLatestVersion(latestVersion)
                        // Determine if the version is up-to-date or not by applying the {@code versionMatchStrategy}
                        // to the request version and the {@code latestVersion}
                        .setIsUpToDate(request.currentVersion().equalsIgnoreCase(latestVersion))
                        .build();
                if (optionalCallback.isPresent()) {
                  optionalCallback.get().onSuccess(response);
                  if (response.isUpToDate()) {
                    optionalCallback.get().onUpToDate(response);
                  } else {
                    optionalCallback.get().onNotUpToDate(response);
                  }
                }
                return Futures.immediateFuture(response);
              }, executor),
          Exception.class,
          cause -> {
            optionalCallback.ifPresent(callback -> callback.onError(cause));
            return Futures.immediateFailedFuture(cause);
          }, executor);
    }
  }
}
