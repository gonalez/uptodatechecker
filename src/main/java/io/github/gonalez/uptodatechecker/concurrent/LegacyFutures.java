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

import com.google.common.util.concurrent.AsyncCallable;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.AsyncFunction;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** Wrapper around {@link Futures} to initially support older versions of Guava's futures. */
@SuppressWarnings("UnstableApiUsage")
public final class LegacyFutures {
  private LegacyFutures() {}

  public static <V> ListenableFuture<V> callAsync(AsyncCallable<V> callable, Executor executor) {
    AwaitingSettableFuture<V> settableFuture = AwaitingSettableFuture.awaiting(executor);
    executor.execute(
        () -> {
          try {
            settableFuture.setFuture(callable.call());
          } catch (Exception e) {
            settableFuture.setException(e);
          }
        });
    return settableFuture;
  }

  public static <V, T extends Throwable> ListenableFuture<V> catchingAsync(
      ListenableFuture<V> input,
      Class<T> exceptionType,
      Function<T, ListenableFuture<V>> fallbackFunction,
      Executor executor) {
    AwaitingSettableFuture<V> settableFuture = AwaitingSettableFuture.awaiting(executor);
    Futures.addCallback(
        input,
        new FutureCallback<>() {
          @Override
          public void onSuccess(V result) {
            settableFuture.setFuture(input);
          }

          @SuppressWarnings("unchecked")
          @Override
          public void onFailure(Throwable t) {
            if (exceptionType.isInstance(t)) {
              settableFuture.setFuture(fallbackFunction.apply((T) t));
            } else settableFuture.setException(t);
          }
        },
        executor);
    return settableFuture;
  }

  public static <V, T> ListenableFuture<T> transformAsync(
      ListenableFuture<V> future, AsyncFunction<V, T> transformFunction, Executor executor) {
    AwaitingSettableFuture<T> settableFuture = AwaitingSettableFuture.awaiting(executor);
    Futures.addCallback(
        callAsync(returningAsyncFuture(future), executor),
        new FutureCallback<>() {
          @Override
          public void onSuccess(V result) {
            try {
              settableFuture.setFuture(transformFunction.apply(result));
            } catch (Exception e) {
              onFailure(e);
            }
          }

          @Override
          public void onFailure(Throwable t) {
            settableFuture.setException(t);
          }
        },
        executor);
    return settableFuture;
  }

  public static <V> ListenableFuture<V> schedulePeriodicAsync(
      AsyncCallable<V> callable, long period, TimeUnit timeUnit, Executor executor) {
    RepeatingCallableFuture<V> repeatingInterruptedCallable =
        new RepeatingCallableFuture<>(callable, period, timeUnit, false);
    executor.execute(repeatingInterruptedCallable);
    return repeatingInterruptedCallable;
  }

  /** @return a {@code AsyncCallable} which returns the given future. */
  private static <V> AsyncCallable<V> returningAsyncFuture(ListenableFuture<V> future) {
    return () -> future;
  }
}
