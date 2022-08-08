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

import static org.junit.jupiter.api.Assertions.*;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.github.gonalez.uptodatechecker.http.HttpClientImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

/** Tests for {@link UpToDateChecker}. */
public class UpToDateCheckerTest {
  // ZNPCs resource id
  static final String RESOURCE_ID = "80940";

  private static UpToDateChecker upToDateChecker;
  
  @BeforeAll
  static void setup() throws Exception {
    upToDateChecker =
        new UpToDateCheckerImpl(
            directExecutor(), new HttpClientImpl(directExecutor()),
            String::equals, Options.newBuilder().build());
  }

  @Test
  public void testInvalidUrlCheckUpToDate() throws Exception {
    ExecutionException executionException =
        assertThrows(
            ExecutionException.class,
            () -> upToDateChecker.checkUpToDate(
                CheckUpToDateRequest.newBuilder()
                    .setUrlToCheck("invalid")
                    .setCurrentVersion("")
                    .build(),
                new UpToDateChecker.Callback(){}).get());

    assertInstanceOf(UpToDateCheckerException.class, executionException.getCause());
  }

  @Test
  public void testMatch() throws Exception {
    assertTrue(checkUpToDateMatching(ApiUrls.SPIGOT_API_URL.apply(RESOURCE_ID), "3.8").get());
  }
  
  @Test
  public void testMismatch() throws Exception {
   assertFalse(checkUpToDateMatching(ApiUrls.SPIGOT_API_URL.apply("0"), "1.0").get());
  }
  
  private ListenableFuture<Boolean> checkUpToDateMatching(String url, String version) {
    return checkUpToDateMatching(CheckUpToDateRequest.newBuilder().setUrlToCheck(url).setCurrentVersion(version).build());
  }
  
  private ListenableFuture<Boolean> checkUpToDateMatching(CheckUpToDateRequest checkUpToDateRequest) {
    SettableFuture<Boolean> matchSettableFuture = SettableFuture.create();
    upToDateChecker.checkUpToDate(checkUpToDateRequest,
        new UpToDateChecker.Callback() {
          @Override
          public void onUpToDate(CheckUpToDateResponse response) {
            matchSettableFuture.set(true);
          }
  
          @Override
          public void onNotUpToDate(CheckUpToDateResponse response) {
            matchSettableFuture.set(false);
          }
  
          @Override
          public void onError(Throwable throwable) {
            matchSettableFuture.set(false);
          }
        });
    return matchSettableFuture;
  }
}
