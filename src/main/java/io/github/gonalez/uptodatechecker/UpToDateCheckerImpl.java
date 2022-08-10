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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A basic, default implementation for {@link UpToDateChecker}.
 *
 * <p>To determine if something is up-to-date or not, we apply the {@code versionMatchStrategy} function to the
 * given request {@link CheckUpToDateRequest#currentVersion() current version} and the latest version of the request
 * determined by the {@link CheckUpToDateRequest#context()}}.
 *
 * <p>{@code latestVersionApiProviderSupplier} supplies the instance of the {@link GetLatestVersionApiProvider} for
 * getting the appropriate {@link GetLatestVersionApi<GetLatestVersionContext>} of the given {@linkplain
 * CheckingUpToDateWithDownloadingAndScheduling#withRequest(String, GetLatestVersionContext) context}
 */
@SuppressWarnings("UnstableApiUsage")
@NotThreadSafe
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final Executor executor;
  private final UpdateDownloader updateDownloader;
  private final Supplier<GetLatestVersionApiProvider> latestVersionApiProviderSupplier;

  public UpToDateCheckerImpl(
      Executor executor,
      UpdateDownloader updateDownloader,
      Supplier<GetLatestVersionApiProvider> latestVersionApiProviderSupplier) {
    this.executor = checkNotNull(executor);
    this.updateDownloader = checkNotNull(updateDownloader);
    this.latestVersionApiProviderSupplier = checkNotNull(latestVersionApiProviderSupplier);

  }

  @Override
  public CheckingUpToDateWithDownloadingAndScheduling checkingUpToDateWithDownloadingAndScheduling() {
    return new CheckingUpToDateWithDownloadingAndSchedulingImpl();
  }

  /** Base implementation for {@link CheckingUpToDateWithDownloadingAndScheduling}. */
  private class CheckingUpToDateWithDownloadingAndSchedulingImpl implements CheckingUpToDateWithDownloadingAndScheduling {
    private final CheckUpToDateRequest.Builder<GetLatestVersionContext> requestBuilder = CheckUpToDateRequest.newBuilder();

    private final SettableFuture<CheckUpToDateResponse> responseSettableFuture = SettableFuture.create();

    CheckingUpToDateWithDownloadingAndScheduling thisInstance() {
      return this;
    }

    @Override
    public CheckingUpToDateWithDownloadingAndScheduling withRequest(String currentVersion, GetLatestVersionContext latestVersionContext) {
      requestBuilder.setCurrentVersion(currentVersion);
      requestBuilder.setContext(latestVersionContext);
      return this;
    }

    @Override
    public CheckingUpToDateWithDownloadingAndScheduling withCallback(@Nullable Callback callback) {
      requestBuilder.setOptionalCallback(Optional.of(checkNotNull(callback)));
      return this;
    }

    @Override
    public DownloadingAndSchedulingOperation<CheckingUpToDateWithDownloadingAndScheduling> then() {
      CheckUpToDateRequest<GetLatestVersionContext> request =
          requestBuilder.build();
      responseSettableFuture.setFuture(checkUpToDate(request, executor));
      return new DownloadingAndSchedulingOperation<>() {
        @Override
        public DownloadingOperation<CheckingUpToDateWithDownloadingAndScheduling> downloading() {
          return computeUpdateDownloaderRequestFunction -> {
            Futures.addCallback(responseSettableFuture, new FutureCallback<>() {
              @Override
              public void onSuccess(CheckUpToDateResponse result) {
                updateDownloader.downloadUpdate(computeUpdateDownloaderRequestFunction.apply(result));
              }

              @Override
              public void onFailure(Throwable t) {}
            }, executor);
            return thisInstance();
          };
        }

        @Override
        public SchedulingOperation<CheckingUpToDateWithDownloadingAndScheduling> scheduling() {
          return new SchedulingOperation<>() {
            @Override
            public CheckingUpToDateWithDownloadingAndScheduling schedule(long period, TimeUnit unit) {
              responseSettableFuture.setFuture(
                  LegacyFutures.schedulePeriodicAsync(
                      () -> checkUpToDate(requestBuilder.build(), executor),
                      period,
                      unit,
                      executor));
              return thisInstance();
            }

            @Override
            public CheckingUpToDateWithDownloadingAndScheduling then() {
              return thisInstance();
            }
          };
        }
      };
    }

    @Override
    public ListenableFuture<CheckUpToDateResponse> response() {
      return responseSettableFuture;
    }

    public <Context extends GetLatestVersionContext> ListenableFuture<CheckUpToDateResponse> checkUpToDate(
        CheckUpToDateRequest<Context> request, Executor executor) {
      Optional<GetLatestVersionApi<Context>> maybeGetProviderForContext =
          latestVersionApiProviderSupplier.get().get(request.context());
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
                        .setNewVersion(latestVersion)
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
