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

public interface CheckUpToDateRequest {
  static Builder newBuilder() {
    return new Builder.DefaultCheckUpToDateRequestBuilder();
  }
  
  String ver();
  String url();
  
  /** Builder for CheckUpToDateRequest. */
  interface Builder {
    Builder setVer(String ver);
    Builder setUrl(String url);
  
    CheckUpToDateRequest build();
    
    final class DefaultCheckUpToDateRequestBuilder implements Builder {
      private String ver, url;
      
      @Override
      public Builder setVer(String ver) {
        this.ver = ver;
        return this;
      }
  
      @Override
      public Builder setUrl(String url) {
        this.url = url;
        return this;
      }
  
      @Override
      public CheckUpToDateRequest build() {
        checkNotNull(ver);
        checkNotNull(url);
        return new CheckUpToDateRequest() {
          @Override
          public String ver() {
            return ver;
          }
  
          @Override
          public String url() {
            return url;
          }
        };
      }
    }
  }
}
