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

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/** Shared values for the UpToDateChecker library. */
@AutoValue
@Immutable
public abstract class Options {
  public static final Options DEFAULT_OPTIONS = Options.newBuilder().build();

  /** @return a new builder to create a {@link Options}. */
  public static Builder newBuilder() {
    return new AutoValue_Options.Builder().setConnectTimeout(10000).setReadTimeout(10000);
  }

  /** @return the timeout for establishing an url connection. */
  public abstract int connectTimeout();

  /** @return the timeout for reading a response. */
  public abstract int readTimeout();

  /** Builder to create {@link Options}s. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the timeout for establishing an url connection. */
    public abstract Builder setConnectTimeout(int connectTimeout);

    /** Sets the timeout for reading a response. */
    public abstract Builder setReadTimeout(int readTimeout);

    /** @return a new {@link Options} based from this builder. */
    public abstract Options build();
  }
}
