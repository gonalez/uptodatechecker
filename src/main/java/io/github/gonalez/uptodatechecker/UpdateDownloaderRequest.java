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

import java.io.File;
import java.nio.file.Path;

/** Request to download a file. */
@AutoValue
@Immutable
public abstract class UpdateDownloaderRequest {
  /** @return a new builder to create a {@link UpdateDownloaderRequest}. */
  public static Builder newBuilder() {
    return new AutoValue_UpdateDownloaderRequest.Builder();
  }

  /** @return the url to download. */
  public abstract String urlToDownload();

  /** @return the path to where the content will be downloaded. */
  public abstract String downloadPath();

  /** Builder for {@link UpdateDownloaderRequest}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the url to download of the request. */
    public abstract Builder setUrlToDownload(String urlToDownload);

    /** Sets the download path of the request. */
    public abstract Builder setDownloadPath(String downloadPath);

    public Builder setDownloadPath(Path path, String fileName) {
      return setDownloadPath(path.toString() + "/" + fileName);
    }

    public Builder setDownloadPath(File file) {
      return setDownloadPath(file.getAbsolutePath());
    }

    /** @return a new {@link UpdateDownloaderRequest} based from this builder. */
    public abstract UpdateDownloaderRequest build();
  }
}
