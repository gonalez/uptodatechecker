package io.github.gonalez.uptodatechecker.providers;

import com.google.auto.value.AutoValue;
import io.github.gonalez.uptodatechecker.VersionProviderContext;

/**
 * Context to get the {@link GithubVersionProvider latest version} of an GitHub repository.
 *
 * @see <a href="https://docs.github.com/en/rest/releases/releases#get-the-latest-release">Get the
 *     latest release</a>.
 */
@AutoValue
public abstract class GithubVersionProviderContext implements VersionProviderContext {
  /** @return a new builder to create a {@link GithubVersionProviderContext}. */
  public static GithubVersionProviderContext.Builder newBuilder() {
    return new AutoValue_GithubVersionProviderContext.Builder();
  }

  /** @return the account owner of the repository. */
  public abstract String repoOwner();

  /** @return the name of the repository. */
  public abstract String repoName();

  /** Builder for {@link GithubVersionProviderContext}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the repository owner of the context. */
    public abstract Builder setRepoOwner(String repoOwner);

    /** Sets the repository name of the context. */
    public abstract Builder setRepoName(String repoName);

    /** @return a new {@link GithubVersionProviderContext} based from this builder. */
    public abstract GithubVersionProviderContext build();
  }
}
