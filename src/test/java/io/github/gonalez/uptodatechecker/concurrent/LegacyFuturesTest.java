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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

/** Tests for {@link LegacyFutures}. */
public class LegacyFuturesTest {
  private static final Executor EXECUTOR = MoreExecutors.directExecutor();

  private static final String BAR = "bar";
  private static final String FOO = "foo";

  @Test
  public void testCatchingAsync() throws Exception {
    ListenableFuture<String> future =
        LegacyFutures.catchingAsync(
            LegacyFutures.callAsync(
                () -> {
                  throw new Exception();
                },
                EXECUTOR),
            Exception.class,
            e -> Futures.immediateFuture(BAR),
            EXECUTOR);
    assertThat(future.get()).isEqualTo(BAR);
  }

  @Test
  public void testTransformAsync() throws Exception {
    ListenableFuture<String> future =
        LegacyFutures.transformAsync(
            Futures.immediateFuture(BAR), s -> Futures.immediateFuture(FOO), EXECUTOR);
    assertThat(future.get()).isEqualTo(FOO);
  }
}
