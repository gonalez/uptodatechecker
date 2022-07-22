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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.function.BiFunction;

/** Provides helper functions for {@link UpToDateChecker}. */
public final class UpToDateCheckerHelper {
  public static final BiFunction<String, String, Boolean> EQUAL_STRATEGY = String::equals;

  /** Returns a immediate, {@code ListenableFuture} whose value is null. */
  public static <V> ListenableFuture<V> immediateNullFuture() {
    return Futures.immediateFuture(null);
  }
  
  private UpToDateCheckerHelper() {}
}
