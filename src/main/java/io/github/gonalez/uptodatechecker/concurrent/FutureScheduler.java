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
package io.github.gonalez.uptodatechecker.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for scheduling {@link Callable}s for a fixed period.
 *
 * <p>Implementations must guarantee that the {@link #schedule(Callable, long, TimeUnit) scheduled future}
 * could be cancelled when calling {@link java.util.concurrent.Future#cancel(boolean)}.
 */
public interface FutureScheduler {
  /**
   * Schedules the given callable for the given period and timeunit.
   *
   * @return a {@link ListenableFuture} representing the current state of the scheduled {@code Callable}.
   */
  <V> ListenableFuture<V> schedule(Callable<V> callable, long period, TimeUnit timeUnit);
}
