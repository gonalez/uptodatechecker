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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * A basic implementation of {@link FutureScheduler} using a {@code ExecutorService}.
 *
 * <p>We use {@link RepeatingCallableFuture} for scheduling.
 */
public class ExecutorFutureScheduler implements FutureScheduler {
  private final Executor executor;
  
  public ExecutorFutureScheduler(Executor executor) {
    this.executor = checkNotNull(executor);
  }
  
  @Override
  public <V> ListenableFuture<V> schedule(Callable<V> callable, long period, TimeUnit timeUnit) {
    RepeatingCallableFuture<V> repeatingInterruptedCallable = new RepeatingCallableFuture<>(callable, period, timeUnit, false);
    executor.execute(repeatingInterruptedCallable);
    return repeatingInterruptedCallable;
  }
}
