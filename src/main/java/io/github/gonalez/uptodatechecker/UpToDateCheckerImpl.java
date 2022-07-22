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
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
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
  private final Executor executor;
  private final UrlBytesReader urlBytesReader;
  private final BiFunction<String, String, Boolean> versionMatchStrategy;
  private final Optional<UpToDateChecker.Callback> optionalCallback;
  
  private final Map<String, ListenableFuture<CheckUpToDateResponse>> cache = new HashMap<>();
  
  public UpToDateCheckerImpl(
      Executor executor,
      UrlBytesReader urlBytesReader,
      BiFunction<String, String, Boolean> versionMatchStrategy) {
    this(executor, urlBytesReader, versionMatchStrategy, Optional.empty());
  }
  
  public UpToDateCheckerImpl(
      Executor executor,
      UrlBytesReader urlBytesReader,
      BiFunction<String, String, Boolean> versionMatchStrategy,
      Optional<UpToDateChecker.Callback> optionalCallback) {
    this.executor = checkNotNull(executor);
    this.urlBytesReader = checkNotNull(urlBytesReader);
    this.versionMatchStrategy = checkNotNull(versionMatchStrategy);
    this.optionalCallback = checkNotNull(optionalCallback);
  }
  
  @Override
  public ListenableFuture<CheckUpToDateResponse> checkUpToDate(CheckUpToDateRequest request, @Nullable Callback callback) {
    ListenableFuture<CheckUpToDateResponse> getFromCache = cache.get(request.urlToCheck());
    if (getFromCache != null) {
      return getFromCache;
    }
    final Callback requestCallback;
    if (optionalCallback.isPresent() && callback != null) {
      requestCallback = Callback.chaining(ImmutableList.of(optionalCallback.get(), callback));
    } else {
      requestCallback = optionalCallback.orElse(callback);
    }
    ListenableFuture<CheckUpToDateResponse> responseFuture =
        FluentFuture.from(Futures.submitAsync(
            () -> {
              try {
                // We create the string based off the read url bytes from the given request url {@code urlToCheck}
                String urlBytesToString = new String(urlBytesReader.readUrlBytes(new URL(request.urlToCheck())));
                // Determine if the version is up-to-date or not applying the function {@code versionMatchStrategy}
                // using the request version against the parsed, url string
                boolean matches = versionMatchStrategy.apply(request.version(), urlBytesToString);
                ListenableFuture<CheckUpToDateResponse> buildResponseFuture = Futures.immediateFuture(
                    CheckUpToDateResponse.newBuilder()
                        .setData(urlBytesToString)
                        .setIsUpToDate(matches)
                        .build());
                if (requestCallback != null) {
                  Futures.addCallback(buildResponseFuture, new FutureCallback<CheckUpToDateResponse>() {
                    @Override
                    public void onSuccess(CheckUpToDateResponse result) {
                      requestCallback.onSuccess(result);
                      if (matches) {
                        requestCallback.onUpToDate(result);
                      } else {
                        requestCallback.onNotUpToDate(result);
                      }
                    }
            
                    @Override
                    public void onFailure(Throwable t) {
                      requestCallback.onError(t);
                    }
                  });
                }
                return buildResponseFuture;
              } catch (MalformedURLException e) {
                // Failed to parse url
                return UpToDateCheckerExceptionCode.INVALID_URL_CODE.toException().toImmediateFailedFuture();
              } catch (IOException e) {
                return UpToDateCheckerExceptionCode.FAIL_TO_READ_URL_BYTES_CODE.toException().toImmediateFailedFuture();
              }
            }, executor))
        .catchingAsync(
            UpToDateCheckerException.class,
            exception -> {
              if (requestCallback != null) {
                requestCallback.onError(exception);
              }
              return Futures.immediateFailedFuture(exception);
            }, executor);
    cache.put(request.urlToCheck(), responseFuture);
    return responseFuture;
  }
}
