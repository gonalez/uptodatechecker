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

import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** A basic, default implementation for {@link UrlBytesReader}. */
public final class UrlBytesReaderImpl implements UrlBytesReader {
  public static final UrlBytesReader INSTANCE = new UrlBytesReaderImpl();
  
  @Override
  public byte[] readUrlBytes(URL url) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream inputStream = url.openConnection().getInputStream()) {
      ByteStreams.copy(inputStream, out);
    }
    return out.toByteArray();
  }
}
