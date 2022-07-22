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
 * A default implementation for {@link UpToDateChecker}. Results are cached to memory
 * identified by the {@link CheckUpToDateRequest#urlToCheck()}.
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
    ListenableFuture<CheckUpToDateResponse> responseListenableFuture = FluentFuture.from(
        Futures.submitAsync(
            () -> {
              try {
                String urlBytesString = new String(urlBytesReader.readUrlBytes(new URL(request.urlToCheck())));
                boolean versionMatch = versionMatchStrategy.apply(request.version(), urlBytesString);
        
                ListenableFuture<CheckUpToDateResponse> future = Futures.immediateFuture(
                    CheckUpToDateResponse.newBuilder()
                        .setData(urlBytesString)
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
                return future;
              } catch (MalformedURLException e) {
                // Failed to parse url
                return UpToDateCheckerException.ofCode(UpToDateCheckerExceptionCode.INVALID_URL_CODE).toImmediateFailedFuture();
              } catch (IOException e) {
                return UpToDateCheckerException.ofCode(UpToDateCheckerExceptionCode.FAIL_TO_READ_URL_BYTES_CODE).toImmediateFailedFuture();
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
    cache.put(request.urlToCheck(), responseListenableFuture);
    return responseListenableFuture;
  }
}
