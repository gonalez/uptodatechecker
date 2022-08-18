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
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A basic, default implementation for {@link UpToDateChecker}.
 *
 * <p>To determine if something is up-to-date or not, we apply the {@code versionMatchStrategy}
 * function to the given request {@link CheckUpToDateRequest#currentVersion() current version} and
 * the latest version of the request determined by the {@link CheckUpToDateRequest#context()}}.
 */
@SuppressWarnings("UnstableApiUsage")
@NotThreadSafe
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final Object lock = new Object();

  private final Executor executor;
  private final Optional<UpdateDownloader> optionalUpdateDownloader;
  private final BiFunction<String, String, Boolean> versionMatchStrategy;

  private final ArrayList<GetLatestVersionApi<? extends GetLatestVersionContext>>
      latestVersionApis = new ArrayList<>();

  public UpToDateCheckerImpl(
      Executor executor,
      Optional<UpdateDownloader> optionalUpdateDownloader,
      BiFunction<String, String, Boolean> versionMatchStrategy) {
    this.executor = checkNotNull(executor);
    this.optionalUpdateDownloader = checkNotNull(optionalUpdateDownloader);
    this.versionMatchStrategy = checkNotNull(versionMatchStrategy);
  }

  @Override
  public CheckingUpToDateWithDownloadingAndScheduling
      checkingUpToDateWithDownloadingAndScheduling() {
    return new CheckingUpToDateWithDownloadingAndSchedulingImpl();
  }

  @Override
  public <Context extends GetLatestVersionContext> ListenableFuture<Void> addLatestVersionApi(
      GetLatestVersionApi<Context> latestVersionApi) {
    return LegacyFutures
        .call(() -> {
          synchronized (lock) {
            latestVersionApis.add(latestVersionApi);
          }
          return null;
        }, executor);
  }

  private <Context extends GetLatestVersionContext> GetLatestVersionApi<Context> getLatestVersionApi(
      Class<? extends GetLatestVersionContext> contextClass) {
    synchronized (lock) {
      for (GetLatestVersionApi<? extends GetLatestVersionContext> api : latestVersionApis) {
        if (api.getContextType().isAssignableFrom(contextClass)) {
          @SuppressWarnings("unchecked") // safe
          GetLatestVersionApi<Context> latestVersionApi =
              (GetLatestVersionApi<Context>) api;
          return latestVersionApi;
        }
      }
    }
    return null;
  }

  /** Base implementation for {@link CheckingUpToDateWithDownloadingAndScheduling}. */
  private class CheckingUpToDateWithDownloadingAndSchedulingImpl
      implements CheckingUpToDateWithDownloadingAndScheduling {
    private final CheckUpToDateRequest.Builder requestBuilder = CheckUpToDateRequest.newBuilder();

    private final List<Function<ListenableFuture<CheckUpToDateResponse>,
        ListenableFuture<CheckUpToDateResponse>>> operations = new ArrayList<>();

    /** @return {@code this}. */
    CheckingUpToDateWithDownloadingAndScheduling thisInstance() {
      return this;
    }

    @Override
    public CheckingUpToDateWithDownloadingAndScheduling requesting(
        CheckUpToDateRequest checkUpToDateRequest) {
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
          operations.add(
              future ->
                  LegacyFutures.schedulePeriodicAsync(
                      () -> {
                        return checkUpToDate(requestBuilder.build(), executor);
                      },
                      period,
                      unit,
                      executor
                  ));
          return thisInstance();
        }

        @Override
        public CheckingUpToDateWithDownloadingAndScheduling download(
            Function<CheckUpToDateResponse, UpdateDownloaderRequest>
                computeUpdateDownloaderRequestFunction) {
          if (!optionalUpdateDownloader.isPresent()) {
            return thisInstance();
          }
          operations.add(
              future ->
                  LegacyFutures.transformAsync(
                      future,
                      response -> {
                        return LegacyFutures.transformAsync(
                            optionalUpdateDownloader.get().downloadUpdate(
                                computeUpdateDownloaderRequestFunction.apply(response)),
                            downloadResult -> {
                              return Futures.immediateFuture(response);
                          },
                          executor);
                      },
                      executor));
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
      Optional<Callback> optionalCallback = request.optionalCallback();

      // Get the latest version (CheckUpToDateResponse#latestVersion)
      ListenableFuture<String> latestVersionFuture =
          LegacyFutures.callAsync(
              () -> {
                GetLatestVersionApi<GetLatestVersionContext> getLatestVersionApi =
                    getLatestVersionApi(request.context().getClass());
                if (getLatestVersionApi == null) {
                  return Futures.immediateFailedFuture(
                      UpToDateCheckerExceptionCode.FAIL_TO_PARSE_VERSION_CODE
                          .toException());
                }
                return getLatestVersionApi.getLatestVersion(request.context());
              },
              executor);

      return LegacyFutures.catchingAsync(
          LegacyFutures.transformAsync(
              latestVersionFuture,
              latestVersion -> {
                CheckUpToDateResponse response =
                    CheckUpToDateResponse.newBuilder()
                        .setLatestVersion(latestVersion)
                        .setIsUpToDate(
                            versionMatchStrategy.apply(request.currentVersion(), latestVersion))
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
              },
              executor),
          Exception.class,
          cause -> {
            optionalCallback.ifPresent(callback -> callback.onError(cause));
            return Futures.immediateFailedFuture(cause);
          },
          executor);
    }
  }
}
