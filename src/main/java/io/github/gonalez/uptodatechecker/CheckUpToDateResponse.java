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

/** Response of {@link CheckUpToDateRequest}. */
@AutoValue
@Immutable
public abstract class CheckUpToDateResponse {
  /** @return a new builder to create a {@link CheckUpToDateResponse}. */
  public static Builder newBuilder() {
    return new AutoValue_CheckUpToDateResponse.Builder();
  }

  /** @return the latest version for the request. */
  public abstract String latestVersion();

  /** @return {@code true} if the request is up-to-date. */
  public abstract boolean isUpToDate();

  /** Builder for {@link CheckUpToDateResponse}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the latest version of the response. */
    public abstract Builder setLatestVersion(String latestVersion);

    /** Sets if the response is up-to-date or not. */
    public abstract Builder setIsUpToDate(boolean isUpToDate);

    /** @return a new {@link CheckUpToDateResponse} based from this builder. */
    public abstract CheckUpToDateResponse build();
  }
}
