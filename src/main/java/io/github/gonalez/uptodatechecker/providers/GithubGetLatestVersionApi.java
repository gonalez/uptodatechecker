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

import com.google.gson.JsonElement;
import io.github.gonalez.uptodatechecker.GetLatestVersionApi;
import io.github.gonalez.uptodatechecker.HttpGetLatestVersionApi;
import io.github.gonalez.uptodatechecker.http.HttpClient;
import io.github.gonalez.uptodatechecker.http.HttpRequest;

import java.util.concurrent.Executor;

/** A {@link GetLatestVersionApi} that can get the latest version of an GitHub repository. */
public class GithubGetLatestVersionApi
    extends HttpGetLatestVersionApi<GithubGetLatestVersionContext> {
  private static final String LATEST_VERSION_URL =
      "https://api.github.com/repos/%s/%s/releases/latest";

  public GithubGetLatestVersionApi(Executor executor, HttpClient httpClient) {
    super(executor, httpClient);
  }

  @Override
  public String name() {
    return "github";
  }

  @Override
  public Class<GithubGetLatestVersionContext> getContextType() {
    return GithubGetLatestVersionContext.class;
  }

  @Override
  protected HttpRequest buildRequest(GithubGetLatestVersionContext context) {
    return HttpRequest.newBuilder()
        .setUrl(String.format(LATEST_VERSION_URL, context.repoOwner(), context.repoName()))
        .build();
  }

  @Override
  protected String readVersion(JsonElement jsonElement) {
    return jsonElement.getAsJsonObject().get("tag_name").getAsString();
  }
}
