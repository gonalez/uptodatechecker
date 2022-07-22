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

import com.google.common.util.concurrent.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

/** Tests for {@link UpToDateChecker}. */
public class UpToDateCheckerTest {
  // ZNPCs resource id api url
  private static final String RESOURCE_API_URL = "https://api.spigotmc.org/legacy/update.php?resource=80940";
  
  private final UpToDateChecker upToDateChecker = new UpToDateCheckerImpl(
      MoreExecutors.directExecutor(), UrlBytesReader.defaultInstance(), String::equals);
  
  @Test
  public void testInvalidUrlCheckUpToDate() throws Exception {
    ExecutionException executionException =
        assertThrows(
            ExecutionException.class,
            () -> upToDateChecker.checkUpToDate(
                CheckUpToDateRequest.newBuilder()
                    .setUrl("")
                    .setVer("")
                    .build(),
            new UpToDateChecker.Callback(){}).get());
    
    assertInstanceOf(UpToDateCheckerException.class, executionException.getCause());
    assertEquals(UpToDateCheckerExceptionCode.INVALID_URL_CODE,
        ((UpToDateCheckerException) executionException.getCause()).getExceptionCode());
  }
  
  @Test
  public void testMatch() throws Exception {
    assertTrue(checkUpToDateMatching(RESOURCE_API_URL, "3.8").get());
  }
  
  @Test
  public void testMismatch() throws Exception {
    assertFalse(checkUpToDateMatching(RESOURCE_API_URL, "1.0").get());
  }
  
  
  private ListenableFuture<Boolean> checkUpToDateMatching(String url, String ver) {
    SettableFuture<Boolean> matchSettableFuture = SettableFuture.create();
    upToDateChecker.checkUpToDate(
        CheckUpToDateRequest.newBuilder()
            .setUrl(url)
            .setVer(ver)
            .build(),
        new UpToDateChecker.Callback() {
          @Override
          public void onMatch(CheckUpToDateResponse response) {
            matchSettableFuture.set(true);
          }
  
          @Override
          public void onMismatch(CheckUpToDateResponse response) {
            matchSettableFuture.set(false);
          }
        });
    return matchSettableFuture;
  }
}
