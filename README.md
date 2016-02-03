# Get it on library
A simple iot library for android that contains basic functions.
Information of matching electronics and other info can be found in a blogpost: <https://medium.com/@christophe.smet1>

It has some basic network scanning and information fetching.
For code examples and a working implementation, check the blogposts or the git repo:
<https://github.com/christophesmet/ledsgetIton>. It's an app that controls WiFi RGB leds, like philips hue etc.

## Example
Initialisation:
```java
//Create the sdk implementation that will talk to.
GetItOn mGetItOn = new GetItOn(mAppContext, loggingService);
//Scan for modules in lan network, returns an observable.
//All network operations are made in RxJava.
//Lan scans are optimized for quick lookups
mGetItOn.scanForLanModules();
//Query the database for cached modules, don't scan the entire subnet each time.
mGetItOn.queryCachedLanModuleForMac(@Nullable String mac);
//And a registrator to register the modules in a smooth flow to your network.
//See the project implementation for more details.
```

If you found a bug, submit a pull request or create an issue :)
Thanks!
