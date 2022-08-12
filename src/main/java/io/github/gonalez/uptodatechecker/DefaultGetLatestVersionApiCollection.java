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

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;

/** Default implementation for {@link GetLatestVersionApiCollection}. */
public class DefaultGetLatestVersionApiCollection implements GetLatestVersionApiCollection {
  private final ImmutableList<GetLatestVersionApi<? extends GetLatestVersionContext>> latestVersionApis;

  public DefaultGetLatestVersionApiCollection(
      ImmutableList<GetLatestVersionApi<? extends GetLatestVersionContext>> latestVersionApis) {
    this.latestVersionApis = checkNotNull(latestVersionApis);
  }

  @Override
  public ImmutableList<GetLatestVersionApi<? extends GetLatestVersionContext>> getLatestVersionApis() {
    return latestVersionApis;
  }
}
