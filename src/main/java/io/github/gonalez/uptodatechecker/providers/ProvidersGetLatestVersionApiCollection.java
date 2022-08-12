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

import com.google.common.collect.ImmutableList;
import io.github.gonalez.uptodatechecker.DefaultGetLatestVersionApiCollection;
import io.github.gonalez.uptodatechecker.http.HttpClient;
import io.github.gonalez.uptodatechecker.HttpGetLatestVersionApi;
import io.github.gonalez.uptodatechecker.GetLatestVersionApiCollection;

import java.util.concurrent.Executor;

/** A {@link GetLatestVersionApiCollection} that supplies {@link HttpGetLatestVersionApi http based} apis of this package. */
public class ProvidersGetLatestVersionApiCollection extends DefaultGetLatestVersionApiCollection {
  public ProvidersGetLatestVersionApiCollection(
      Executor executor, HttpClient httpClient) {
    super(ImmutableList.of(
        new GithubGetLatestVersionApi(executor, httpClient),
        new SpigetGetLatestVersionApi(executor, httpClient)));
  }
}
