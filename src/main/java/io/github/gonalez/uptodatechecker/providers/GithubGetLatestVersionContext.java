package io.github.gonalez.uptodatechecker.providers;

import com.google.auto.value.AutoValue;
import io.github.gonalez.uptodatechecker.GetLatestVersionContext;

/**
 * Context to get the {@link GithubGetLatestVersionApi latest version} of an GitHub repository.
 *
 * @see <a href="https://docs.github.com/en/rest/releases/releases#get-the-latest-release">Get the
 *     latest release</a>.
 */
@AutoValue
public abstract class GithubGetLatestVersionContext implements GetLatestVersionContext {
  /** @return a new builder to create a {@link GithubGetLatestVersionContext}. */
  public static GithubGetLatestVersionContext.Builder newBuilder() {
    return new AutoValue_GithubGetLatestVersionContext.Builder();
  }

  /** @return the account owner of the repository. */
  public abstract String repoOwner();

  /** @return the name of the repository. */
  public abstract String repoName();

  /** Builder for {@link GithubGetLatestVersionContext}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the repository owner of the context. */
    public abstract Builder setRepoOwner(String repoOwner);

    /** Sets the repository name of the context. */
    public abstract Builder setRepoName(String repoName);

    /** @return a new {@link GithubGetLatestVersionContext} based from this builder. */
    public abstract GithubGetLatestVersionContext build();
  }
}
