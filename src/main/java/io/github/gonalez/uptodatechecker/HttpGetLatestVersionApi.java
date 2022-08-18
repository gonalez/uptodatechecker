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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.gonalez.uptodatechecker.concurrent.LegacyFutures;
import io.github.gonalez.uptodatechecker.http.HttpClient;
import io.github.gonalez.uptodatechecker.http.HttpRequest;

import java.util.concurrent.Executor;

/**
 * An {@link GetLatestVersionApi} that make http requests for getting the {@link
 * #getLatestVersion(GetLatestVersionContext)}.
 */
public abstract class HttpGetLatestVersionApi<T extends GetLatestVersionContext>
    implements GetLatestVersionApi<T> {
  private static final JsonParser JSON_PARSER = new JsonParser();

  private final Executor executor;
  private final HttpClient httpClient;

  public HttpGetLatestVersionApi(Executor executor, HttpClient httpClient) {
    this.executor = checkNotNull(executor);
    this.httpClient = checkNotNull(httpClient);
  }

  protected abstract HttpRequest buildRequest(T context);

  protected abstract String readVersion(JsonElement jsonElement);

  @Override
  public ListenableFuture<String> getLatestVersion(T context) {
    return LegacyFutures.catchingAsync(
        LegacyFutures.transformAsync(
            httpClient.requestAsync(buildRequest(context)),
            response -> {
              JsonElement jsonElement;
              try {
                jsonElement = JSON_PARSER.parse(response.bodyString());
              } catch (JsonSyntaxException jsonSyntaxException) {
                jsonElement = new JsonPrimitive(response.bodyString());
              }
              return Futures.immediateFuture(readVersion(jsonElement));
            },
            executor),
        Exception.class,
        cause -> {
          return Futures.immediateFailedFuture(cause);
        },
        executor);
  }
}
