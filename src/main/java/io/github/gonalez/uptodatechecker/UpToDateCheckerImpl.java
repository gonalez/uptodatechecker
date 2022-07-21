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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executor;

@SuppressWarnings("UnstableApiUsage")
public class UpToDateCheckerImpl implements UpToDateChecker {
  private final Executor executor;
  
  public UpToDateCheckerImpl(Executor executor) {
    this.executor = checkNotNull(executor);
  }
  
  @Override
  public ListenableFuture<CheckUpToDateRequest> checkUpToDate(CheckUpToDateRequest request) {
    URL url;
    try {
      url = new URL(request.url());
    } catch (MalformedURLException malformedURLException) {
      return Futures.immediateFailedFuture(
          UpToDateCheckerException.newBuilder()
              .setExceptionCode(UpToDateCheckerExceptionCode.INVALID_URL_CODE)
              .build());
    }
    return Futures.submitAsync(
        () -> {
          return null;
        }, executor);
  }
}
