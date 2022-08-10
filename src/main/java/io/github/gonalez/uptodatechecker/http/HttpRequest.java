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
import java.util.Optional;

/** Request of {@link HttpResponse}. */
@AutoValue
@Immutable
public abstract class HttpRequest {
  /** @return the url to perform the request. */
  public abstract String url();

  /** @return the options to be used for the request. */
  public abstract Options options();

  /** @return a new builder to create a {@link HttpRequest}. */
  public static Builder newBuilder() {
    return new AutoValue_HttpRequest.Builder().setOptions(Options.DEFAULT_OPTIONS);
  }

  /** Builder for {@link HttpRequest}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setUrl(String url);
    public abstract Builder setOptions(Options options);

    /** @return a new {@link HttpRequest} from this builder. */
    public abstract HttpRequest build();
  }
}
