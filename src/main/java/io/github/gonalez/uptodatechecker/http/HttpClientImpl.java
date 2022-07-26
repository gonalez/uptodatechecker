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
package io.github.gonalez.uptodatechecker.http;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.github.gonalez.uptodatechecker.UpToDateCheckerExceptionCode;
import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;

/** A basic implementation for {@link HttpClient} which uses {@link HttpURLConnection}. */
@SuppressWarnings("UnstableApiUsage")
public class HttpClientImpl implements HttpClient {
  private final Executor executor;

  public HttpClientImpl(Executor executor) {
    this.executor = checkNotNull(executor);
  }

  @Override
  public ListenableFuture<HttpResponse> requestAsync(HttpRequest request) {
    final URL url;
    try {
      url = new URL(request.url());
    } catch (MalformedURLException e) {
      return Futures.immediateFailedFuture(
          UpToDateCheckerExceptionCode.INVALID_URL_CODE.toException());
    }
    return LegacyFutures.catchingAsync(
        LegacyFutures.callAsync(
            () -> {
              HttpURLConnection urlConnection = null;
              try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(request.options().connectTimeout());
                urlConnection.setReadTimeout(request.options().readTimeout());

                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(true);

                int responseCode;
                try {
                  urlConnection.connect();
                  responseCode = urlConnection.getResponseCode();
                } catch (IOException e) {
                  return Futures.immediateFailedFuture(
                      UpToDateCheckerExceptionCode.FAIL_TO_CONNECT_CODE.toException());
                }
                HttpResponse.Builder builder =
                    HttpResponse.newBuilder().setResponseCode(responseCode);
                try (InputStream input = urlConnection.getInputStream()) {
                  builder.setBody(ByteStreams.toByteArray(input));
                }
                return Futures.immediateFuture(builder.build());
              } finally {
                if (urlConnection != null) {
                  urlConnection.disconnect();
                }
              }
            },
            executor),
        Exception.class,
        Futures::immediateFailedFuture,
        executor);
  }
}
