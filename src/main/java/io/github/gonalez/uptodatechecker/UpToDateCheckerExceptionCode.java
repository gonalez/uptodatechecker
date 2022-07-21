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

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;

/** Enumerates all possible error codes of an {@link UpToDateCheckerException}. */
public enum UpToDateCheckerExceptionCode {
  
  INVALID_URL_CODE(300);
  
  private static final ImmutableMap<Integer, UpToDateCheckerExceptionCode> CODES_TO_EXCEPTION_CODE;
  static {
    ImmutableMap.Builder<Integer, UpToDateCheckerExceptionCode> builder = ImmutableMap.builder();
    for (UpToDateCheckerExceptionCode code : values()) {
      builder.put(code.errorCode, code);
    }
    CODES_TO_EXCEPTION_CODE = builder.build();
  }
  
  @Nullable
  public static UpToDateCheckerExceptionCode fromErrorCode(int errorCode) {
    return CODES_TO_EXCEPTION_CODE.get(errorCode);
  }
  
  final int errorCode;
  
  UpToDateCheckerExceptionCode(int errorCode) {
    this.errorCode = errorCode;
  }
}