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

import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/** Builder for the {@link UpToDateChecker}. */
public class UpToDateCheckerBuilder {
  /** Creates a new {@link UpToDateCheckerBuilder}. */
  public static UpToDateCheckerBuilder newBuilder() {
    return new UpToDateCheckerBuilder();
  }

  private final ImmutableList.Builder<VersionProvider
      <? extends VersionProviderContext>> versionProviderBuilder = ImmutableList.builder();
  private Optional<UpdateDownloader> optionalUpdateDownloader = Optional.empty();
  private BiFunction<String, String, Boolean> versionMatchStrategy = String::equals;

  private Executor executor;

  public UpToDateCheckerBuilder() {}

  public UpToDateCheckerBuilder setExecutor(Executor executor) {
    this.executor = executor;
    return this;
  }

  public UpToDateCheckerBuilder setOptionalUpdateDownloader(
      Optional<UpdateDownloader> optionalUpdateDownloader) {
    this.optionalUpdateDownloader = optionalUpdateDownloader;
    return this;
  }

  public UpToDateCheckerBuilder setVersionMatchStrategy(
      BiFunction<String, String, Boolean> versionMatchStrategy) {
    this.versionMatchStrategy = versionMatchStrategy;
    return this;
  }

  public <T extends VersionProviderContext> UpToDateCheckerBuilder addVersionProvider(
      VersionProvider<T> versionProvider) {
    versionProviderBuilder.add(versionProvider);
    return this;
  }

  public UpToDateChecker build() {
    checkNotNull(optionalUpdateDownloader);
    checkNotNull(versionMatchStrategy);
    checkNotNull(executor);

    ImmutableList<VersionProvider<? extends VersionProviderContext>> versionProviders =
        versionProviderBuilder.build();
    UpToDateChecker upToDateChecker = new UpToDateCheckerImpl(
        executor, optionalUpdateDownloader, versionMatchStrategy);
    for (VersionProvider<? extends VersionProviderContext> versionProvider : versionProviders) {
      if (versionProvider != null) {
        upToDateChecker.addVersionProvider(versionProvider);
      }
    }
    return upToDateChecker;
  }
}
