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
import java.util.Optional;

/** Request to check for up-to-date an url. */
@AutoValue
@Immutable
public abstract class CheckUpToDateRequest {
  /** @return the context that will be used to get the latest version. */
  public abstract GetLatestVersionContext context();

  /**
   * Returns the current version that will be checked against the latest version to
   * check if the response is up-to-date or not.
   */
  public abstract String currentVersion();

  /** @return the optional callback to invoke when executing the up-to-date checker. */
  public abstract Optional<UpToDateChecker.Callback> optionalCallback();

  /** @return a new builder to create a {@link CheckUpToDateRequest}. */
  public static CheckUpToDateRequest.Builder newBuilder() {
    return new AutoValue_CheckUpToDateRequest.Builder()
        .setOptionalCallback(Optional.empty());
  }

  /** Builder for {@link CheckUpToDateRequest}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setContext(GetLatestVersionContext context);
    public abstract Builder setCurrentVersion(String currentVersion);

    public abstract Builder setOptionalCallback(Optional<UpToDateChecker.Callback> optionalCallback);

    /** @return a new {@link CheckUpToDateRequest} from this builder. */
    public abstract CheckUpToDateRequest  build();
  }
}
