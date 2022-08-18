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
package io.github.gonalez.uptodatechecker.providers;

import com.google.auto.value.AutoValue;
import io.github.gonalez.uptodatechecker.VersionProviderContext;

/** Context to get the {@link SpigetVersionProvider latest version} of a Spigot resource. */
@AutoValue
public abstract class SpigetVersionProviderContext implements VersionProviderContext {
  /** @return a new builder to create a {@link SpigetVersionProviderContext}. */
  public static SpigetVersionProviderContext.Builder newBuilder() {
    return new AutoValue_SpigetVersionProviderContext.Builder();
  }

  /** @return the spigot resource-id to get the latest version for. */
  public abstract String resourceId();

  /** Builder for {@link SpigetVersionProviderContext}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the spigot resource-id that will be used to get the latest version. */
    public abstract Builder setResourceId(String resourceId);

    /** @return a new {@link SpigetVersionProviderContext} from this builder. */
    public abstract SpigetVersionProviderContext build();
  }
}
