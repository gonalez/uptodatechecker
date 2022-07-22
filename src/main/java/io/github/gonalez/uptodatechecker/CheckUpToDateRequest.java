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

/** Request to check for up-to-date an url. */
public interface CheckUpToDateRequest {
  static Builder newBuilder() {
    return new Builder.DefaultCheckUpToDateRequestBuilder();
  }
  
  String urlToCheck();
  String version();
  
  /** Builder for {@link CheckUpToDateRequest}. */
  interface Builder {
    Builder setUrlToCheck(String url);
    Builder setVersion(String version);
    
    CheckUpToDateRequest build();
    
    final class DefaultCheckUpToDateRequestBuilder implements Builder {
      private String urlToCheck, version;
  
      @Override
      public Builder setUrlToCheck(String urlToCheck) {
        this.urlToCheck = urlToCheck;
        return this;
      }
  
      @Override
      public Builder setVersion(String version) {
        this.version = version;
        return this;
      }
      
      @Override
      public CheckUpToDateRequest build() {
        checkNotNull(urlToCheck);
        checkNotNull(version);
        return new CheckUpToDateRequest() {
          @Override
          public String urlToCheck() {
            return urlToCheck;
          }
  
          @Override
          public String version() {
            return version;
          }
        };
      }
    }
  }
}
