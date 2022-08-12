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

import com.google.common.util.concurrent.ListenableFuture;

/** Responsible for providing the latest version for a given request. */
public interface GetLatestVersionApi<Context extends GetLatestVersionContext> {
  /** @return the name of this provider. */
  String name();

  /**
   * Returns the type of context that this {@code GetLatestVersionApi} uses to get the
   * {@link #getLatestVersion(GetLatestVersionContext) latest version}.
   */
  Class<Context> getContextType();

  /** @return a {@code ListenableFuture<String>} containing the latest version of the given request. */
  ListenableFuture<String> getLatestVersion(Context context);
}
