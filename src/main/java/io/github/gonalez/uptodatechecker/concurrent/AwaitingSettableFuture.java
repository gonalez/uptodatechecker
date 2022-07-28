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

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link AbstractFuture} which acts like as a {@code SettableFuture} except that it controls
 * how long we can wait to have the set value of the {@code SettableFuture}.
 */
@SuppressWarnings("UnstableApiUsage")
public final class AwaitingSettableFuture<V> extends AbstractFuture<V> implements Runnable {
  public static <V> AwaitingSettableFuture<V> awaiting(Executor executor) {
    return new AwaitingSettableFuture<>(0, TimeUnit.SECONDS, executor);
  }
  
  private final CountDownLatch futureInProgressLatch = new CountDownLatch(1);
  
  private final int maxDelay;
  private final TimeUnit timeUnit;
  
  /**
   * Creates a AwaitingSettableFuture with the given specifications.
   *
   * @param maxDelay how long we can wait to have the set value.
   * @param timeUnit unit of time for {@code maxDelay}.
   * @param executor executor used for the future to latch countdown when we have the value.
   */
  public AwaitingSettableFuture(int maxDelay, TimeUnit timeUnit, Executor executor) {
    this.maxDelay = maxDelay;
    this.timeUnit = checkNotNull(timeUnit);
    checkNotNull(executor);
    addListener(futureInProgressLatch::countDown, executor);
  }
  
  @Override
  public void run() {
    try {
      boolean cancelled = false;
      if (maxDelay > 0)
        cancelled = futureInProgressLatch.await(maxDelay, timeUnit);
      else
        futureInProgressLatch.await();
      if (!cancelled && !isDone()) {
        setException(new TimeoutException(
            String.format(
                "Future timed out after %d %s", maxDelay, timeUnit.toString().toLowerCase())));
      }
    } catch (InterruptedException interruptedException) {
      setException(interruptedException);
    }
  }
  
  @Override
  public boolean set(V value) {
    return super.set(value);
  }
  
  @Override
  public boolean setException(Throwable throwable) {
    return super.setException(throwable);
  }
  
  @Override
  public boolean setFuture(ListenableFuture<? extends V> future) {
    return super.setFuture(future);
  }
}
