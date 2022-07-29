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

import com.google.common.util.concurrent.AbstractFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link AbstractFuture} that is also a runnable, the logic to repeat the future is behind the {@link #run()} method.
 * This blocks the thread in which it was called until is {@link #cancel(boolean) cancelled}.
 *
 * <p>{@code callable} is the callable to be called on each repetition, we
 * wait {@code period} using the {@code timeUnit} to go for the next repetition.
 */
final class RepeatingCallableFuture<V> extends AbstractFuture<V> implements Runnable {
  private final Callable<V> callable;
  private final long period;
  private final TimeUnit timeUnit;
  private final boolean shouldCancelOnFailure;
  
  private final AtomicBoolean cancelled = new AtomicBoolean();

  public RepeatingCallableFuture(
      Callable<V> callable, long period, TimeUnit timeUnit, boolean shouldCancelOnFailure) {
    this.callable = callable;
    this.period = period;
    this.timeUnit = timeUnit;
    this.shouldCancelOnFailure = shouldCancelOnFailure;
  }
  
  @Override
  public void run() {
    while (!isCancelled()) {
      try {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        timeUnit.sleep(period);
        if (isCancelled()) {
          cancel(false); break;
        }
        V call = callable.call();
        set(call);
      } catch (InterruptedException interruptedException) {
        cancel(false);
      } catch (Throwable throwable) {
        if (throwable instanceof ExecutionException) {
          setException(throwable.getCause());
        } else {
          setException(throwable);
        }
        if (shouldCancelOnFailure) {
          cancel(false);
        }
      }
    }
  }
  
  @Override
  public boolean isCancelled() {
    return cancelled.get();
  }
  
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return cancelled.compareAndSet(false, true);
  }
}