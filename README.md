UpToDateChecker
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
    implementation 'io.github.gonalez.uptodatechecker:uptodatechecker:+'
}
```
### Maven
```xml
<dependency>
   <groupId>io.github.gonalez.uptodatechecker</groupId>
   <artifactId>uptodatechecker</artifactId>
   <version>LATEST</version>
</dependency>
```

## Example
Here's a short example that shows how to check if a spigot resource is up-to-date or not.

```java
public class ExampleClass {
  
  public static void main(String[] args) {
    UpToDateChecker upToDateChecker = 
        UpToDateChecker.of(
            /*sync*/MoreExecutors.newDirectExecutorService(),
            UrlBytesReader.defaultInstance(), 
            UpToDateCheckerHelper.EQUAL_STRATEGY,
            Optional.empty());
    
    // Create the request to check if the resource is up-to-date
    ListenableFuture<CheckUpToDateResponse> responseListenableFuture =
        upToDateChecker.checkUpToDate(
            CheckUpToDateRequest.newBuilder()
                .setApiUrl(ApiUrls.SPIGOT_API_URL.apply(/*spigotResourceId*/))
                .setCurrentVersion(/*versionToMatch*/)
                .build(),
            new UpToDateChecker.Callback() {
              @Override
              public void onUpToDate(CheckUpToDateResponse response) {
                // called if the version is up-to-date
              }
              
              @Override
              public void onNotUpToDate(CheckUpToDateResponse response) {
                // called if the version is not up-to-date
              }
        });
  }
}
```
### Fluent Builder Calls API
A builder to make calls (requests) to an UpToDateChecker easier.
```java
Cancellable cancellable =
    FluentUpToDateCheckerCall.newCall(
        CheckUpToDateRequest.newBuilder()
            .setApiUrl(ApiUrls.SPIGOT_API_URL.apply(/*spigotResourceId*/))
            .setCurrentVersion(/*versionToMatch*/)
            .build())
        .setShutdownOnCancel(true)
        .scheduling(12, TimeUnit.HOURS) // Every how often we should check again?
    .start();
```
## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0