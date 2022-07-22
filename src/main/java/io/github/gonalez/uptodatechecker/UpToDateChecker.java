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

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * The entry point of the UpToDateChecker library.
 */
public interface UpToDateChecker {
  /** Functions to be called when calling {@link #checkUpToDate(CheckUpToDateRequest, Callback)}. */
  interface Callback {
    static Callback chaining(Iterable<Callback> callbacks) {
      return new ChainingUpToDateCheckerCallback(callbacks);
    }
    
    /** Called whenever a response has been completed with no errors. */
    default void onSuccess(CheckUpToDateResponse response) {}
    
    /** Called after {@link #onSuccess(CheckUpToDateResponse)}. */
    default void onUpToDate(CheckUpToDateResponse response) {}
    
    /** Called after {@link #onSuccess(CheckUpToDateResponse)}. */
    default void onNotUpToDate(CheckUpToDateResponse response) {}
    
    /** Called if any error occurs. */
    default void onError(Throwable throwable) {}
    
    final class ChainingUpToDateCheckerCallback implements Callback {
      private final Iterable<Callback> callbacks;
      
      public ChainingUpToDateCheckerCallback(Iterable<Callback> callbacks) {
        this.callbacks = requireNonNull(callbacks);
      }
  
      @Override
      public void onSuccess(CheckUpToDateResponse response) {
        for (Callback callback : callbacks) {
          callback.onSuccess(response);
        }
      }
  
      @Override
      public void onUpToDate(CheckUpToDateResponse response) {
        for (Callback callback : callbacks) {
          callback.onUpToDate(response);
        }
      }
  
      @Override
      public void onNotUpToDate(CheckUpToDateResponse response) {
        for (Callback callback : callbacks) {
          callback.onNotUpToDate(response);
        }
      }
      
      @Override
      public void onError(Throwable throwable) {
        for (Callback callback : callbacks) {
          callback.onError(throwable);
        }
      }
    }
  }
  
  /** Creates a new {@link UpToDateChecker} based on the given specifications. */
  static UpToDateChecker of(
      Executor executor, UrlBytesReader urlBytesReader,
      BiFunction<String, String, Boolean> matchStrategy, Optional<Callback> optionalCallback) {
    return new UpToDateCheckerImpl(executor, urlBytesReader, matchStrategy, optionalCallback);
  }
  
  /**
   * Asynchronously checks if the given request is up-to-date based on the request url and version.
   *
   * @param request the request.
   * @param callback the optional callback to execute when the future has been completed.
   * @return a future to a {@link CheckUpToDateResponse} representing the result of the given request.
   */
  ListenableFuture<CheckUpToDateResponse> checkUpToDate(CheckUpToDateRequest request, @Nullable Callback callback);
}
