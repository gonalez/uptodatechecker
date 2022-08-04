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

/** Response of {@link CheckUpToDateRequest}. */
public interface CheckUpToDateResponse {
  /** Creates a new builder to create a {@link CheckUpToDateResponse}. */
  static Builder newBuilder() {
    return new Builder.DefaultCheckUpToDateResponseBuilder();
  }
  
  /** @return a new {@link CheckUpToDateRequest} from the given data and isUpToDate. */
  static CheckUpToDateResponse of(String newVersion, boolean isUpToDate) {
    return newBuilder().setNewVersion(newVersion).setIsUpToDate(isUpToDate).build();
  }
  
  String newVersion();
  boolean isUpToDate();
  
  /** Builder for {@link CheckUpToDateResponse}. */
  interface Builder {
    Builder setNewVersion(String newVersion);
    Builder setIsUpToDate(boolean isUpToDate);
  
    /** @return a new {@link CheckUpToDateResponse} based from this builder. */
    CheckUpToDateResponse build();
    
    final class DefaultCheckUpToDateResponseBuilder implements Builder {
      private String newVersion;
      private boolean upToDate;
      
      @Override
      public Builder setNewVersion(String newVersion) {
        this.newVersion = newVersion;
        return this;
      }
  
      @Override
      public Builder setIsUpToDate(boolean isUpToDate) {
        this.upToDate = isUpToDate;
        return this;
      }
  
      @Override
      public CheckUpToDateResponse build() {
        checkNotNull(newVersion);
        return new CheckUpToDateResponse() {
          @Override
          public String newVersion() {
            return newVersion;
          }
  
          @Override
          public boolean isUpToDate() {
            return upToDate;
          }
        };
      }
    }
  }
}
