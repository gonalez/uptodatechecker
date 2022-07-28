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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Function;

/** Wrapper around {@link Futures} to initially support older versions of Guava's futures. */
public final class LegacyFutures {
  
  public static <V> ListenableFuture<V> submitAsync(Callable<V> callable, Executor executor) {
    AwaitingSettableFuture<V> settableFuture = AwaitingSettableFuture.awaiting(executor);
    executor.execute(() -> {
      try {
        settableFuture.set(callable.call());
      } catch (Exception e) {
        settableFuture.setException(e);
      }
    });
    return settableFuture;
  }
  
  public static <V, T extends Throwable> ListenableFuture<V> catchingAsync(
      ListenableFuture<V> input, Class<T> exceptionType,
      Function<T, ListenableFuture<V>> fallbackFunction, Executor executor) {
    AwaitingSettableFuture<V> settableFuture = AwaitingSettableFuture.awaiting(executor);
    Futures.addCallback(input, new FutureCallback<>() {
      @Override
      public void onSuccess(V result) {
        settableFuture.setFuture(input);
      }
  
      @SuppressWarnings("unchecked")
      @Override
      public void onFailure(Throwable t) {
        if (exceptionType.isInstance(t)) {
          settableFuture.setFuture(fallbackFunction.apply((T)t));
        }
         else
           settableFuture.setException(t);
      }
    });
    return settableFuture;
  }
  
  public static <V, T> ListenableFuture<T> transformAsync(
      Callable<V> callable, Function<V, ListenableFuture<T>> transformFunction, Executor executor) {
    AwaitingSettableFuture<T> settableFuture = AwaitingSettableFuture.awaiting(executor);
    Futures.addCallback(submitAsync(callable, executor), new FutureCallback<>() {
      @Override
      public void onSuccess(V result) {
        settableFuture.setFuture(transformFunction.apply(result));
      }
  
      @Override
      public void onFailure(Throwable t) {
        settableFuture.setException(t);
      }
    });
    return settableFuture;
  }
  
  /** Returns an immediate, {@code ListenableFuture} whose value is null. */
  public static <V> ListenableFuture<V> immediateNullFuture() {
    return Futures.immediateFuture(null);
  }
  
  private LegacyFutures() {}
}
