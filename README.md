UpToDateChecker ![build](https://github.com/gonalez/uptodatechecker/workflows/build/badge.svg) ![release](https://img.shields.io/github/release/gonalez/uptodatechecker.svg) ![license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
============================
UpToDateChecker is a small and basic library for checking if something is up-to-date or not.

## Include in your build

### Gradle

```gradle
repositories {
    // ...
    mavenCentral()
}

dependencies {
    implementation 'io.github.gonalez.uptodatechecker:uptodatechecker:0.0.2'
}
```

### Maven

```xml
<dependency>
   <groupId>io.github.gonalez.uptodatechecker</groupId>
   <artifactId>uptodatechecker</artifactId>
   <version>0.0.2</version>
</dependency>
```

## Creating a UpToDateChecker
The first step to make use of the API is to create an instance of 
the `UpToDateChecker`, this is the entry point of the library.

```java
UpToDateChecker upToDateChecker = UpToDateCheckerBuilder.newBuilder()
    .setExecutor(MoreExecutors.directExecutor())
    .setVersionMatchStrategy(String::equals)
    .build();
```

### Update Downloading
One of the best features of the API is the ability to download updates when a 
request is not up-to-date, this can be done by setting an `UpdateDownloader` to the
`UpToDateChecker`, you can use the `setOptionalUpdateDownloader` method for this.

The library provides a default implementation of the `UpdateDownloader` called 
`FileUpdateDownloader`, where the updates will be automatically downloaded to 
the request path.

```java
new FileUpdateDownloader(executor, httpClient, options);
```
## Register a Version Provider
The API provides a method called `addVersionProvider`, this can be used to register a version
provider into the `UpToDateChecker`. A version provider is responsible for obtaining the 
latest version of something i.e. a GitHub repository, for the request context. 
```java
upToDateChecker.addVersionProvider(new GithubVersionProvider(executor, httpClient));
```

## Creating the request
To check for up-to-date something, you first must create an `CheckUpToDateRequest` instance.
We determine if the version is up-to-date by applying the `versionMatchStrategy` to the
`currentVersion` of the request to the latest version of the `VersionProvider`.

There must be a version provider registered for the given request context type, otherwise the
API won't know what version we're trying to compare against the request, current version.

```java
CheckUpToDateRequest request = CheckUpToDateRequest.newBuilder()
       .setContext(
           GithubVersionProviderContext.newBuilder()
             .setRepoName("uptodatechecker")
             .setRepoOwner("gonalez")
             .build())
       .setCurrentVersion("0.0.2")
       .build();
```

### Callback
You can also set a callback to the request to listen when we get the latest version or if there are any errors, etc.

```java
request.setOptionalCallback(Optional.of(new UpToDateChecker.Callback() {
      @Override
      public void onUpToDate(CheckUpToDateResponse response) {
          // called if the response is up-to-date
      }

      @Override
      public void onNotUpToDate(CheckUpToDateResponse response) {
          // called if the response is not up-to-date
      }
}));
```

## Send the request
Finally, you can send the request to check if the request is up-to-date.

```java
ListenableFuture<CheckUpToDateResponse> response = 
    upToDateChecker.checkWithDownloadingAndScheduling()
        .requesting(request)
        .response();
assertTrue(response.isUpToDate());
```

### Operation Chaining
You can combine several operations to be executed when checking for up-to-date something by using the
[`ThenOperation`]. This example uses the `download` operation to download the update if the request is not up-to-date 
(for this to work you must register an `UpdateDownloader` as explained in the [Update Downloading](#update-downloading) section):

```java
upToDateChecker.checkWithDownloadingAndScheduling()
    .requesting(request)
    .then()
    .download(downloadRequest)
    // ...
    .response();
```

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

[`ThenOperation`]: https://github.com/gonalez/uptodatechecker/blob/master/src/main/java/io/github/gonalez/uptodatechecker/UpToDateChecker.java#L77