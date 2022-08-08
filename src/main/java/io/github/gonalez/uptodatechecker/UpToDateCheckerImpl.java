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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;
import io.github.gonalez.uptodatechecker.http.HttpClient;
import io.github.gonalez.uptodatechecker.http.HttpRequest;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * A basic implementation for {@link UpToDateChecker}.
 *
 * <p>To determine if something is up-to-date or not, we use the {@code versionMatchStrategy} function, and then
 * we compare it with the given request {@link CheckUpToDateRequest#currentVersion()} and the parsed, string content of
 * the request {@link CheckUpToDateRequest#urlToCheck}.
 *
 * <p>{@code optionalCallback} is an optional callback that can be chained with the callback given at
 * {@link #checkUpToDate(CheckUpToDateRequest, Callback)} if non-null, otherwise use just optionalCallback.
 */
@SuppressWarnings("UnstableApiUsage")
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final Executor executor;
  private final HttpClient httpClient;
  private final BiFunction<String, String, Boolean> versionMatchStrategy;
  private final Optional<UpToDateChecker.Callback> optionalCallback;
  private final Options options;

  public UpToDateCheckerImpl(
      Executor executor,
      HttpClient httpClient,
      BiFunction<String, String, Boolean> versionMatchStrategy,
      Options options) {
    this(executor, httpClient, versionMatchStrategy, Optional.empty(), options);
  }
  
  public UpToDateCheckerImpl(
      Executor executor,
      HttpClient httpClient,
      BiFunction<String, String, Boolean> versionMatchStrategy,
      Optional<UpToDateChecker.Callback> optionalCallback,
      Options options) {
    this.executor = checkNotNull(executor);
    this.httpClient = checkNotNull(httpClient);
    this.versionMatchStrategy = checkNotNull(versionMatchStrategy);
    this.optionalCallback = checkNotNull(optionalCallback);
    this.options = checkNotNull(options);
  }
  
  @Override
  public ListenableFuture<CheckUpToDateResponse> checkUpToDate(CheckUpToDateRequest request, @Nullable Callback callback) {
    final Callback requestCallback;
    if (optionalCallback.isPresent() && callback != null) {
      requestCallback = Callback.chaining(ImmutableList.of(optionalCallback.get(), callback));
    } else {
      requestCallback = optionalCallback.orElse(callback);
    }
    return LegacyFutures.catchingAsync(
        LegacyFutures.transformAsync(
            httpClient.requestAsync(HttpRequest.of(request.urlToCheck(), options)),
            httpResponse -> {
              String body = httpResponse.body();
              if (request.versionExtractor().isPresent()) {
                body = request.versionExtractor().get().extractVersion(body);
              }
              CheckUpToDateResponse response =
                  CheckUpToDateResponse.newBuilder()
                      .setNewVersion(body)
                      // Determine if the version is up-to-date or not by applying the {@code versionMatchStrategy}
                      // to the request version and the parsed, url response {@code body}
                      .setIsUpToDate(versionMatchStrategy.apply(request.currentVersion(), body))
                      .build();
              if (requestCallback != null) {
                requestCallback.onSuccess(response);
                if (response.isUpToDate()) {
                  requestCallback.onUpToDate(response);
                } else {
                  requestCallback.onNotUpToDate(response);
                }
              }
              return Futures.immediateFuture(response);
            }, executor),
        Exception.class,
        cause -> {
          if (requestCallback != null) {
            requestCallback.onError(cause);
          }
          return Futures.immediateFailedFuture(cause);
        }, executor);
  }
}
