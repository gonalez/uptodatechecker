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

import java.util.Optional;

/** Request to check for up-to-date an url. */
public interface CheckUpToDateRequest {
  /** Creates a new builder to create a {@link CheckUpToDateRequest}. */
  static Builder newBuilder() {
    return new Builder.DefaultCheckUpToDateRequestBuilder();
  }
  
  /** @return a new {@link CheckUpToDateRequest} from the given apiUrl and version. */
  static CheckUpToDateRequest of(String apiUrl, String currentVersion) {
    return newBuilder()
        .setApiUrl(apiUrl)
        .setCurrentVersion(currentVersion)
        .build();
  }
  
  String apiUrl();
  String currentVersion();
  
  Optional<VersionExtractor> versionExtractor();
  
  /** Builder for {@link CheckUpToDateRequest}. */
  interface Builder {
    Builder setApiUrl(String url);
    Builder setCurrentVersion(String currentVersion);
    Builder setVersionExtractor(Optional<VersionExtractor> versionExtractor);
  
    /** @return a new {@link CheckUpToDateRequest} based from this builder. */
    CheckUpToDateRequest build();
    
    final class DefaultCheckUpToDateRequestBuilder implements Builder {
      private String apiUrl;
      private String currentVersion;
      private Optional<VersionExtractor> versionExtractor = Optional.empty();
  
      @Override
      public Builder setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
      }
  
      @Override
      public Builder setCurrentVersion(String version) {
        this.currentVersion = version;
        return this;
      }
  
      @Override
      public Builder setVersionExtractor(Optional<VersionExtractor> versionExtractor) {
        this.versionExtractor = versionExtractor;
        return this;
      }
  
      @Override
      public CheckUpToDateRequest build() {
        checkNotNull(apiUrl);
        checkNotNull(currentVersion);
        checkNotNull(versionExtractor);
        return new CheckUpToDateRequest() {
          @Override
          public String apiUrl() {
            return apiUrl;
          }
  
          @Override
          public String currentVersion() {
            return currentVersion;
          }
  
          @Override
          public Optional<VersionExtractor> versionExtractor() {
            return versionExtractor;
          }
        };
      }
    }
  }
}
