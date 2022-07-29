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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Tests for {@link AwaitingSettableFuture}. */
public class AwaitingSettableFutureTest {
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
  
  @Test
  public void testAwaitingSuccess() throws Exception {
    AwaitingSettableFuture<String> awaitingDoneFuture = AwaitingSettableFuture.awaiting(MoreExecutors.directExecutor());
    EXECUTOR_SERVICE.submit(awaitingDoneFuture);
    awaitingDoneFuture.set("foo");
  }
  
  @Test
  public void testAwaitingFail() throws Exception {
    AwaitingSettableFuture<String> awaitingDoneFuture = new AwaitingSettableFuture<>(5, TimeUnit.SECONDS, MoreExecutors.directExecutor());
    EXECUTOR_SERVICE.submit(awaitingDoneFuture);
    
    ExecutionException executionException = assertThrows(ExecutionException.class, awaitingDoneFuture::get);
    assertInstanceOf(TimeoutException.class, executionException.getCause());
  }
}
