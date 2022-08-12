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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

/** Responsible for providing {@link GetLatestVersionApi}s for {@link GetLatestVersionContext}s. */
public interface GetLatestVersionApiProvider {
  /** @return a new {@link GetLatestVersionApiProvider} of the given {@code latestVersionApis}. */
  static GetLatestVersionApiProvider of(ImmutableList<GetLatestVersionApiCollection> collections) {
    GetLatestVersionApiProvider.Builder builder = newBuilder();
    for (GetLatestVersionApiCollection collection : collections) {
      for (GetLatestVersionApi<? extends GetLatestVersionContext> latestVersionApi
          : collection.getLatestVersionApis()) {
        builder.addLatestVersionProviderApi(latestVersionApi);
      }
    }
    return builder.build();
  }

  /** @return a new builder to create a {@link GetLatestVersionApiProvider}. */
  static GetLatestVersionApiProvider.Builder newBuilder() {
    return new Builder.GetLatestVersionApiProviderImpl();
  }

  /** Returns the {@link GetLatestVersionApi} for the given context. */
  <Context extends GetLatestVersionContext> Optional<GetLatestVersionApi<Context>> get(Context context);

  /** Build for {@link GetLatestVersionApiProvider}. */
  interface Builder {
    <Context extends GetLatestVersionContext> Builder addLatestVersionProviderApi(
        Class<Context> contextClass, GetLatestVersionApi<Context> providerApi);

    default <Context extends GetLatestVersionContext> Builder addLatestVersionProviderApi(
        GetLatestVersionApi<Context> providerApi) {
      return addLatestVersionProviderApi(providerApi.getContextType(), providerApi);
    }


    /** @return a new {@link GetLatestVersionApiProvider} from this builder. */
    GetLatestVersionApiProvider build();

    public final class GetLatestVersionApiProviderImpl implements GetLatestVersionApiProvider.Builder {
      private final ImmutableMap.Builder
          <Class<? extends GetLatestVersionContext>, GetLatestVersionApi<?>> builder = ImmutableMap.builder();

      @Override
      public <T extends GetLatestVersionContext> Builder addLatestVersionProviderApi(Class<T> type, GetLatestVersionApi<T> request) {
        builder.put(type, request);
        return this;
      }

      @Override
      public GetLatestVersionApiProvider build() {
        return new GetLatestVersionApiProvider() {
          final ImmutableMap<Class<? extends GetLatestVersionContext>, GetLatestVersionApi<?>> build = builder.build();

          @Override
          public <Context extends GetLatestVersionContext> Optional<GetLatestVersionApi<Context>> get(Context context) {
            if (!build.containsKey(context.getClass().getSuperclass())) {
              return Optional.empty();
            }
            for (Map.Entry<Class<? extends GetLatestVersionContext>,
                GetLatestVersionApi<?>> latestVersionApiEntry : build.entrySet()) {
              if (latestVersionApiEntry.getKey().isInstance(context)) {
                @SuppressWarnings("unchecked") // safe
                GetLatestVersionApi<Context> latestVersionApi =
                    (GetLatestVersionApi<Context>) latestVersionApiEntry.getValue();
                return Optional.of(latestVersionApi);
              }
            }
            return Optional.empty();
          }
        };
      }
    }
  }
}
