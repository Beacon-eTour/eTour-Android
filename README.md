# eTour Android app
This is an Android app for eTour. Utilizing [AndroidBeaconLibrary](https://altbeacon.github.io/android-beacon-library/)

## Overview

Besides running the the Flutter module, the app requests the location permission, registers the Flutter event channel ```etour_event_channel```, and starts monitoring iBeacon with the UUID ```B9407F30-F5F8-466E-AFF9-25556B57FE6D``` which is currently hardcoded.

## Integration to the Flutter module

These instructions follow the guidelines from official Flutter docs, if you run into any problems refer to this link: 
https://flutter.dev/docs/development/add-to-app/ios/project-setup

1. Follow instructions on the [Flutter module repo](https://github.com/Beacon-eTour/eTour-flutter) to run that.
2. Clone this repo. Note that the project is setup so that this project and the Flutter module should live in the same root folder like this:
```some/path/
├── eTour-flutter/
└── eTour-android/
```
3. Install Android Studio (if you don't have that yet)
4. Open the project with Android Studio
