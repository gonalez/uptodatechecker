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

/** The response for a {@link CheckUpToDateRequest}. */
public interface CheckUpToDateResponse {
  static Builder newBuilder() {
    return new Builder.DefaultCheckUpToDateResponseBuilder();
  }
  
  String data();
  boolean isUpToDate();
  
  /** Builder for {@link CheckUpToDateResponse}. */
  interface Builder {
    Builder setData(String data);
    Builder setIsUpToDate(boolean isUpToDate);
  
    CheckUpToDateResponse build();
    
    final class DefaultCheckUpToDateResponseBuilder implements Builder {
      private String data;
      private boolean upToDate;
      
      @Override
      public Builder setData(String data) {
        this.data = data;
        return this;
      }
  
      @Override
      public Builder setIsUpToDate(boolean isUpToDate) {
        this.upToDate = isUpToDate;
        return this;
      }
  
      @Override
      public CheckUpToDateResponse build() {
        checkNotNull(data);
        return new CheckUpToDateResponse() {
          @Override
          public String data() {
            return data;
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
