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

/** Something that can be cancelled. */
@FunctionalInterface
public interface Cancellable {
  static Cancellable chaining(Iterable<Cancellable> cancellables) {
    return new ChainingCancellable(cancellables);
  }
  
  void cancel();
  
  final class ChainingCancellable implements Cancellable {
    private final Iterable<Cancellable> cancellables;
    
    public ChainingCancellable(Iterable<Cancellable> cancellables) {
      this.cancellables = cancellables;
    }
    
    @Override
    public void cancel() {
      for (Cancellable cancellable : cancellables) {
        cancellable.cancel();
      }
    }
  }
}