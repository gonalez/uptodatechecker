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
import com.google.common.util.concurrent.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
  private final ExecutorService executorService;
  private final UrlBytesReader urlBytesReader;
  private final BiFunction<String, String, Boolean> versionMatchStrategy;
  private final Optional<UpToDateChecker.Callback> optionalCallback;
  
  private final Map<String, ListenableFuture<CheckUpToDateResponse>> cache = new HashMap<>();
  
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
    this.executorService = checkNotNull(executorService);
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
    return FluentFuture.from(
        Futures.submitAsync(
            () -> {
              if (cache.containsKey(request.urlToCheck())) {
                return cache.get(request.urlToCheck());
              }
              try {
                // We create the string based off the read url-bytes from the given request {@code urlToCheck}
                String urlBytesToString = new String(urlBytesReader.readUrlBytes(new URL(request.urlToCheck())));
                // Determine if the version is up-to-date or not applying the function {@code versionMatchStrategy}
                // using the request version against the parsed, url string
                boolean versionMatch = versionMatchStrategy.apply(request.version(), urlBytesToString);
                ListenableFuture<CheckUpToDateResponse> future = Futures.immediateFuture(
                    CheckUpToDateResponse.newBuilder()
                        .setData(urlBytesToString)
                        .setIsUpToDate(versionMatch)
                        .build());
                if (requestCallback != null) {
                  Futures.addCallback(future, new FutureCallback<CheckUpToDateResponse>() {
                    @Override
                    public void onSuccess(CheckUpToDateResponse result) {
                      requestCallback.onSuccess(result);
                      if (versionMatch) {
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
                cache.put(request.urlToCheck(), future);
                return future;
              } catch (MalformedURLException e) {
                // Failed to parse url
                return UpToDateCheckerException.ofCode(UpToDateCheckerExceptionCode.INVALID_URL_CODE).toImmediateFailedFuture();
              } catch (IOException e) {
                return UpToDateCheckerException.ofCode(UpToDateCheckerExceptionCode.FAIL_TO_READ_URL_BYTES_CODE).toImmediateFailedFuture();
              }
            }, executorService))
        .catchingAsync(
            UpToDateCheckerException.class,
            exception -> {
              if (requestCallback != null) {
                requestCallback.onError(exception);
              }
              return Futures.immediateFailedFuture(exception);
              }, executorService);
  }
  
  @Override
  public ListenableFuture<Void> shutdown() {
    executorService.shutdownNow();
    return immediateNullFuture();
  }
  
  @Override
  public void clear() {
    executorService.execute(cache::clear);
  }
}
