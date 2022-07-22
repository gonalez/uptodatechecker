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

import com.google.common.base.Function;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

@SuppressWarnings("UnstableApiUsage")
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final Executor executor;
  private final UrlBytesReader urlBytesReader;
  private final BiFunction<String, String, Boolean> versionMatchStrategy;
  
  public UpToDateCheckerImpl(
      Executor executor,
      UrlBytesReader urlBytesReader,
      BiFunction<String, String, Boolean> versionMatchStrategy) {
    this.executor = checkNotNull(executor);
    this.urlBytesReader = checkNotNull(urlBytesReader);
    this.versionMatchStrategy = checkNotNull(versionMatchStrategy);
  }
  
  @Override
  public ListenableFuture<CheckUpToDateResponse> checkUpToDate(CheckUpToDateRequest request, @Nullable Callback callback) {
    URL url;
    try {
      url = new URL(request.url());
    } catch (MalformedURLException malformedURLException) {
      return Futures.immediateFailedFuture(UpToDateCheckerException.ofCode(UpToDateCheckerExceptionCode.INVALID_URL_CODE));
    }
    return Futures.submitAsync(
        () -> {
          String urlBytesString;
          try {
            urlBytesString = new String(urlBytesReader.readUrlBytes(url));
          } catch (IOException e) {
            return Futures.immediateFailedFuture(UpToDateCheckerException.ofCode(UpToDateCheckerExceptionCode.FAIL_TO_READ_URL_BYTES_CODE));
          }
          boolean versionMatch = versionMatchStrategy.apply(request.ver(), urlBytesString);
          ListenableFuture<CheckUpToDateResponse> stringListenableFuture = Futures.immediateFuture(new CheckUpToDateResponse() {
            @Override
            public String data() {
              return urlBytesString;
            }
          });
          if (callback != null) {
            Futures.addCallback(stringListenableFuture, new FutureCallback<CheckUpToDateResponse>() {
              @Override
              public void onSuccess(@NullableDecl CheckUpToDateResponse result) {
                callback.onSuccess(result);
                if (versionMatch) {
                  callback.onMatch(result);
                } else {
                  callback.onMismatch(result);
                }
              }
  
              @Override
              public void onFailure(Throwable t) {
                callback.onError(t);
              }
            });
          }
          return stringListenableFuture;
        }, executor);
  }
}
