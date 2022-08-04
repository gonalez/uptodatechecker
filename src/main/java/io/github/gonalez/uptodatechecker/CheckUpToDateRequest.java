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
  
  /** @return a new {@link CheckUpToDateRequest} from the given url and version. */
  static CheckUpToDateRequest of(String urlToCheck, String currentVersion) {
    return newBuilder().setUrlToCheck(urlToCheck).setCurrentVersion(currentVersion).build();
  }
  
  String urlToCheck();
  String currentVersion();
  
  Optional<VersionExtractor> versionExtractor();
  
  /** Builder for {@link CheckUpToDateRequest}. */
  interface Builder {
    Builder setUrlToCheck(String urlToCheck);
    Builder setCurrentVersion(String currentVersion);
    Builder setVersionExtractor(Optional<VersionExtractor> versionExtractor);
  
    /** @return a new {@link CheckUpToDateRequest} based from this builder. */
    CheckUpToDateRequest build();
    
    final class DefaultCheckUpToDateRequestBuilder implements Builder {
      private String urlToCheck, currentVersion;
      private Optional<VersionExtractor> versionExtractor = Optional.empty();
  
      @Override
      public Builder setUrlToCheck(String urlToCheck) {
        this.urlToCheck = urlToCheck;
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
        checkNotNull(urlToCheck);
        checkNotNull(currentVersion);
        checkNotNull(versionExtractor);
        return new CheckUpToDateRequest() {
          @Override
          public String urlToCheck() {
            return urlToCheck;
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
