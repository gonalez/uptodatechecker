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

import io.github.gonalez.uptodatechecker.http.HttpClient;
import io.github.gonalez.uptodatechecker.providers.GithubGetLatestVersionApi;
import io.github.gonalez.uptodatechecker.providers.GithubGetLatestVersionContext;
import io.github.gonalez.uptodatechecker.providers.SpigetGetLatestVersionApi;
import io.github.gonalez.uptodatechecker.providers.SpigetGetLatestVersionContext;

import java.util.concurrent.Executor;

/** Default implementation for {@link GetLatestVersionApiProviderSupplier}. */
public class LibGetLatestVersionApiProviderSupplier implements GetLatestVersionApiProviderSupplier {
  private final GetLatestVersionApiProvider latestVersionApiProvider;

  public LibGetLatestVersionApiProviderSupplier(Executor executor, HttpClient httpClient) {
    checkNotNull(executor);
    checkNotNull(httpClient);
    this.latestVersionApiProvider =
        GetLatestVersionApiProvider.newBuilder()
            .addLatestVersionProviderApi(GithubGetLatestVersionContext.class, new GithubGetLatestVersionApi(executor, httpClient))
            .addLatestVersionProviderApi(SpigetGetLatestVersionContext.class, new SpigetGetLatestVersionApi(executor, httpClient))
            .build();
  }

  @Override
  public GetLatestVersionApiProvider get() {
    return latestVersionApiProvider;
  }
}
