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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link Callable} which can be retried multiple times until is {@link Cancellable#cancel() cancelled},
 * this implementation is synchronous.
 *
 * <p>{@code callable} is the default callable to be called on each retry, we wait {@code period} using the
 * {@code timeUnit} to go for the next retry.
 */
final class RetryingCallable<V> implements Callable<V>, Cancellable {
  private final Callable<V> callable;
  private final long period;
  private final TimeUnit timeUnit;
  
  private final AtomicBoolean cancelled = new AtomicBoolean(false);
  
  public RetryingCallable(
      Callable<V> callable,
      long period,
      TimeUnit timeUnit) {
    this.callable = callable;
    this.period = period;
    this.timeUnit = timeUnit;
  }
  
  @Override
  public V call() throws Exception {
    V call = callable.call();
    while (true) {
      if (cancelled.get()) {
        break;
      }
      try {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        timeUnit.sleep(period);
        call = callable.call();
      } catch (Exception exception) {
        break;
      }
    }
    return call;
  }
  
  @Override
  public void cancel() {
    cancelled.compareAndSet(false, true);
  }
}