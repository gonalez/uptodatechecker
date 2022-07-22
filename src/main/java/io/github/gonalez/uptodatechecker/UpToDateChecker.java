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

import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;

public interface UpToDateChecker {
  /** Functions to be called when calling {@link #checkUpToDate(CheckUpToDateRequest, Callback)}. */
  interface Callback {
    /** Called whenever a response has been completed. */
    default void onSuccess(CheckUpToDateResponse response) {}
    
    /** Called after {@link #onSuccess(CheckUpToDateResponse)}. */
    default void onMatch(CheckUpToDateResponse response) {}
    
    /** Called after {@link #onSuccess(CheckUpToDateResponse)}. */
    default void onMismatch(CheckUpToDateResponse response) {}
    
    /** Called if any error occurs. */
    default void onError(Throwable throwable) {}
  }
  
  ListenableFuture<CheckUpToDateResponse> checkUpToDate(CheckUpToDateRequest request, @Nullable Callback callback);
}
