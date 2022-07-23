UpToDateChecker
============================
UpToDateChecker is a small and basic library for checking if something is up-to-date or not, based on a request containing an url and a version.

## Examples
The easiest way to check if something is up-to-date, in this example a spigot resource:

```java
public class ExampleClass  {
  
  public static void main(String[] args) {
    UpToDateChecker upToDateChecker = 
        UpToDateChecker.of(
            MoreExecutors.newDirectExecutorService(),
            UrlBytesReader.defaultInstance(), 
            UpToDateCheckerHelper.EQUAL_STRATEGY,
            Optional.empty());
    
    // Create the request to check if the resource is up-to-date
    ListenableFuture<CheckUpToDateResponse> responseListenableFuture =
        upToDateChecker.checkUpToDate(
            CheckUpToDateRequest.newBuilder()
                .setUrlToCheck(ApiUrls.SPIGOT_API_URL.apply(/*spigotResourceId*/))
                .setVersion(/*versionToMatch*/)
                .build(), new UpToDateChecker.Callback() {
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

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0