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

import java.util.function.Function;

/** Responsible for extracting a valid and readable version for a given string. */
// TODO: Explain better the docs of this.
@FunctionalInterface
public interface VersionExtractor {
  /**
   * A {@link VersionExtractor} which does nothing more than return the given passed,
   * string when calling {@link #extractVersion(String)}.
   */
  VersionExtractor NO_OP = content -> content;
  
  /** Creates a new {@link VersionExtractor} from the given function. */
  static VersionExtractor of(Function<String, String> function) {
    return function::apply;
  }
  
  /** Extracts the version from the given string. */
  String extractVersion(String urlContent);
}
