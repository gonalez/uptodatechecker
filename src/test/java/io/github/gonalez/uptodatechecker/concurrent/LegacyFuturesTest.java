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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;

/** Tests for {@link LegacyFutures}. */
public class LegacyFuturesTest {
  private static final String BAR = "bar";
  private static final String FOO = "foo";
  
  @Test
  public void testCatchingAsync() throws Exception {
    ListenableFuture<String> future =
        LegacyFutures.catchingAsync(
            LegacyFutures.callAsync(() -> {
              throw new Exception();
              }, MoreExecutors.directExecutor()),
            Exception.class,
            e -> Futures.immediateFuture(BAR), MoreExecutors.directExecutor());
    assertEquals(BAR, future.get());
  }
  
  @Test
  public void testTransformAsync() throws Exception {
    ListenableFuture<String> future =
        LegacyFutures.transformAsync(
            Futures.immediateFuture(BAR),
            s -> Futures.immediateFuture(FOO),
            MoreExecutors.directExecutor());
    assertEquals(FOO, future.get());
  }
}
