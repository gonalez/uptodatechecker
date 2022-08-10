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
import javax.annotation.concurrent.Immutable;
import java.nio.charset.StandardCharsets;

/** Response of {@link HttpClient#requestAsync(HttpRequest)}. */
@AutoValue
@Immutable
public abstract class HttpResponse {
  /** @return the body of the response. */
  public abstract byte[] body();

  public String bodyString() {
    return new String(body(), StandardCharsets.UTF_8);
  }

  /** @return the code of the response. */
  public abstract int responseCode();

  /** @return a new builder to create a {@link HttpResponse}. */
  public static HttpResponse.Builder newBuilder() {
    return new AutoValue_HttpResponse.Builder();
  }

  /** Builder for {@link HttpResponse}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setBody(byte[] body);

    public abstract Builder setResponseCode(int responseCode);
    public abstract HttpResponse build();
  }
}
