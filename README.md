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
    implementation 'io.github.gonalez.uptodatechecker:uptodatechecker:0.0.1'
}
```

### Maven

```xml
<dependency>
   <groupId>io.github.gonalez.uptodatechecker</groupId>
   <artifactId>uptodatechecker</artifactId>
   <version>0.0.1</version>
</dependency>
```

## Example

Here's a short example that shows how to check if a spigot resource is up-to-date or not.

```java
import java.util.concurrent.TimeUnit;

public class ExampleClass {

  public static void main(String[] args) {
    UpToDateChecker upToDateChecker =
        new UpToDateCheckerImpl(executor,
            Optional.of(
                new FileUpdateDownloader(executor, httpClient, Options.DEFAULT_OPTIONS)),
            String::equals);
    upToDateChecker.addLatestVersionApi(new SpigetGetLatestVersionApi(executor, httpClient));
    
    String resourceId = "...";
    
    // Create the request to be checked to see if the resource is up-to-date
    CheckUpToDateRequest checkUpToDateRequest =
        CheckUpToDateRequest.newBuilder()
            .setContext(
                SpigetGetLatestVersionContext.newBuilder()
                    .setResourceId(resourceId)
                    .build())
            .setCurrentVersion(currentResourceVersion)
            .setOptionalCallback(Optional.of(new UpToDateChecker.Callback() {
              @Override
              public void onUpToDate(CheckUpToDateResponse response) {
                // called if the response is up-to-date
              }

              @Override
              public void onNotUpToDate(CheckUpToDateResponse response) {
                // called if the response is not up-to-date
              }
            }))
            .build();

    ListenableFuture<CheckUpToDateResponse> responseFuture = 
        upToDateChecker.checkingUpToDateWithDownloadingAndScheduling()
            .requesting(checkUpToDateRequest)
            .then()
            .download(response ->
                UpdateDownloaderRequest.newBuilder()
                    .setUrlToDownload(
                        DownloadingUrls.SPIGET_DOWNLOAD_UPDATE_FILE_URL.apply(resourceId))
                    .setDownloadPath(
                        temporaryDirectory, 
                        String.format("update-%s.jar", response.latestVersion()))
                    .build())
            .response();
    assertTrue(response.isUpToDate());
  }
}
```

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0