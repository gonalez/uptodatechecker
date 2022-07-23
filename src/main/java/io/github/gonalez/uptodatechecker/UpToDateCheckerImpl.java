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
import static io.github.gonalez.uptodatechecker.UpToDateCheckerHelper.immediateNullFuture;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * A basic implementation for {@link UpToDateChecker}. Results are cached to memory identified by
 * the {@link CheckUpToDateRequest#urlToCheck()}.
 *
 * <p>To determine if something is up-to-date or not, we use the {@code versionMatchStrategy} function, and then
 * we compare it with the given request {@link CheckUpToDateRequest#version()} and the parsed, string content of
 * the request {@link CheckUpToDateRequest#urlToCheck() url}.
 *
 * <p>{@code optionalCallback} is an optional callback that can be chained with the callback given at
 * {@link #checkUpToDate(CheckUpToDateRequest, Callback)} if non-null, otherwise use just optionalCallback.
 */
@SuppressWarnings("UnstableApiUsage")
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final ListeningExecutorService executorService;
  private final UrlBytesReader urlBytesReader;
  private final BiFunction<String, String, Boolean> versionMatchStrategy;
  private final Optional<UpToDateChecker.Callback> optionalCallback;
  
  private final ConcurrentHashMap<String, ListenableFuture<CheckUpToDateResponse>> futuresCache = new ConcurrentHashMap<>();
  
  public UpToDateCheckerImpl(
      ExecutorService executorService,
      UrlBytesReader urlBytesReader,
      BiFunction<String, String, Boolean> versionMatchStrategy) {
    this(executorService, urlBytesReader, versionMatchStrategy, Optional.empty());
  }
  
  public UpToDateCheckerImpl(
      ExecutorService executorService,
      UrlBytesReader urlBytesReader,
      BiFunction<String, String, Boolean> versionMatchStrategy,
      Optional<UpToDateChecker.Callback> optionalCallback) {
    this.executorService = MoreExecutors.listeningDecorator(executorService);
    this.urlBytesReader = checkNotNull(urlBytesReader);
    this.versionMatchStrategy = checkNotNull(versionMatchStrategy);
    this.optionalCallback = checkNotNull(optionalCallback);
  }
  
  @Override
  public ListenableFuture<CheckUpToDateResponse> checkUpToDate(CheckUpToDateRequest request, @Nullable Callback callback) {
    final Callback requestCallback;
    if (optionalCallback.isPresent() && callback != null) {
      requestCallback = Callback.chaining(ImmutableList.of(optionalCallback.get(), callback));
    } else {
      requestCallback = optionalCallback.orElse(callback);
    }
    ListenableFuture<CheckUpToDateResponse> responseListenableFuture =
        executorService.submit(() -> {
          String urlContentToString = UpToDateCheckerHelper.urlContentToString(urlBytesReader, request.urlToCheck());
          CheckUpToDateResponse response =
              CheckUpToDateResponse.of(
                  urlContentToString,
                  // Determine if the version is up-to-date or not by applying the function {@code versionMatchStrategy}
                  // to the request version and the parsed, url string {@code urlContentToString}
                  versionMatchStrategy.apply(request.version(), urlContentToString));
          if (requestCallback != null) {
            requestCallback.onSuccess(response);
            if (response.isUpToDate()) {
              requestCallback.onUpToDate(response);
            } else {
              requestCallback.onNotUpToDate(response);
            }
          }
          return response;
        });
    return Futures.catchingAsync(
        responseListenableFuture,
        Exception.class,
        cause -> {
          if (requestCallback != null) {
            requestCallback.onError(cause);
          }
          return Futures.immediateFailedFuture(cause);
        }, executorService);
  }
  
  @Override
  public ListenableFuture<Void> shutdown() {
    executorService.shutdownNow();
    return immediateNullFuture();
  }
  
  @Override
  public void clear() {
    futuresCache.clear();
  }
}
