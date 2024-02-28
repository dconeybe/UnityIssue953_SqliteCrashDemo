# Android Test App to Reproduce SQLiteDatabaseLockedException Crash

See https://github.com/firebase/firebase-unity-sdk/issues/953

The application is configured to use the Firestore emulator.
To use the emulator, start it on your computer by running

```
firebase emulators:start --only firestore
```

To use a production Firebase project, do the following:

1. Copy your `google-services.json` into the `app` subdirectory
   (replacing the dummy `google-services.json` that is there).
2. Edit the `google-services.json` to set the "package_name"
   to "com.google.dconeybe".
3. Uncheck the "Use Firestore Emulator" button in the app's UI
   and restart the application.
