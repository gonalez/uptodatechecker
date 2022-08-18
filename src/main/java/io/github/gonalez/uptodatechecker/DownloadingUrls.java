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

import java.util.function.Function;

/**
 * Common downloading urls to be used for the {@link
 * io.github.gonalez.uptodatechecker.UpdateDownloaderRequest#urlToDownload()}.
 */
public enum DownloadingUrls implements Function<String, String> {
  // File downloading APIs
  SPIGET_DOWNLOAD_UPDATE_FILE_URL("https://api.spiget.org/v2/resources/%s/download");

  final String apiUrl;

  private DownloadingUrls(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  @Override
  public String apply(String s) {
    return String.format(apiUrl, s);
  }
}
