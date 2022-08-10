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

import com.google.auto.value.AutoValue;
import io.github.gonalez.uptodatechecker.Options;

import javax.annotation.concurrent.Immutable;

/** Request of {@link HttpResponse}. */
@AutoValue
@Immutable
public abstract class HttpRequest {
  /** @return a new {@link HttpRequest} for the given specifications. */
  public static HttpRequest of(String url, Options options) {
    return newBuilder().setUrl(url)
        .setConnectTimeout(options.connectTimeout())
        .setReadTimeout(options.readTimeout())
        .build();
  }

  public abstract String url();

  public abstract int connectTimeout();

  public abstract int readTimeout();

  /** @return a new builder to create a {@link HttpRequest}. */
  public static Builder newBuilder() {
    return new AutoValue_HttpRequest.Builder();
  }

  /** Builder for {@link HttpRequest}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setUrl(String url);
    public abstract Builder setConnectTimeout(int connectTimeout);
    public abstract Builder setReadTimeout(int readTimeout);

    public abstract HttpRequest build();
  }
}
