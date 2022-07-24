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

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/** The request to download a file. */
public interface UpdateDownloaderRequest {
  static Builder newBuilder() {
    return new Builder.DefaultUpdateDownloaderRequest();
  }
  
  String urlToDownload();
  String downloadPath();
  
  /** Builder to create {@link UpdateDownloaderRequest}s. */
  interface Builder {
    Builder setUrlToDownload(String urlToDownload);
    Builder setDownloadPath(String downloadPath);
  
    default Builder setDownloadPath(Path path, String fileName) {
      return setDownloadPath(path.toString() + "/" + fileName);
    }
  
    default Builder setDownloadPath(File file) {
      return setDownloadPath(file.getAbsolutePath());
    }
    
    UpdateDownloaderRequest build();
    
    final class DefaultUpdateDownloaderRequest implements Builder {
      private String urlToDownload, downloadPath;
      
      @Override
      public Builder setUrlToDownload(String urlToDownload) {
        this.urlToDownload = urlToDownload;
        return this;
      }
  
      @Override
      public Builder setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
      }
  
      @Override
      public UpdateDownloaderRequest build() {
        checkNotNull(urlToDownload);
        checkNotNull(downloadPath);
        return new UpdateDownloaderRequest() {
          @Override
          public String urlToDownload() {
            return urlToDownload;
          }
  
          @Override
          public String downloadPath() {
            return downloadPath;
          }
        };
      }
    }
  }
}
