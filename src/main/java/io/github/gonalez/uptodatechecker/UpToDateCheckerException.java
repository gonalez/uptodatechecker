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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/** Exception thrown by {@link UpToDateChecker} when any error occurs. */
public class UpToDateCheckerException extends Exception {
  /** Creates a new builder to create a {@link UpToDateCheckerException}. */
  public static Builder newBuilder() {
    return new Builder();
  }
  
  public static UpToDateCheckerException ofCode(UpToDateCheckerExceptionCode code) {
    return new UpToDateCheckerException(code);
  }
  
  private final UpToDateCheckerExceptionCode exceptionCode;
  
  public UpToDateCheckerException(UpToDateCheckerExceptionCode exceptionCode) {
    super();
    this.exceptionCode = checkNotNull(exceptionCode);
  }
  
  public UpToDateCheckerException(String message, UpToDateCheckerExceptionCode exceptionCode) {
    super(message);
    this.exceptionCode = checkNotNull(exceptionCode);
  }
  
  public UpToDateCheckerException(String message, Throwable cause, UpToDateCheckerExceptionCode exceptionCode) {
    super(message, cause);
    this.exceptionCode = checkNotNull(exceptionCode);
  }
  
  public UpToDateCheckerExceptionCode getExceptionCode() {
    return exceptionCode;
  }
  
  public <V> ListenableFuture<V> toImmediateFailedFuture() {
    return Futures.immediateFailedFuture(this);
  }
  
  public static final class Builder {
    private UpToDateCheckerExceptionCode exceptionCode;
  
    Builder() {}
    
    public Builder setExceptionCode(UpToDateCheckerExceptionCode exceptionCode) {
      this.exceptionCode = exceptionCode;
      return this;
    }
    
    public UpToDateCheckerException build() {
      checkNotNull(exceptionCode);
      return new UpToDateCheckerException(exceptionCode);
    }
  }
}
