# F-Droid Build Notes

This document describes how to reproduce a reproducible / F-Droid-compatible build of **FoxAppMemo**.

---

## Requirements

| Tool | Version |
|------|---------|
| JDK | 17 (temurin or adoptium) |
| Android SDK | Platform 35, Build-Tools 35.x |
| Gradle (wrapper) | 8.x (provided via `gradlew`) |

No NDK or CMake is required.

---

## Permissions

The only declared permission is:

```xml
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

This is needed on Android 11+ to enumerate installed applications via `PackageManager`.

**There is no `INTERNET` permission.** The app never makes network requests; all data is stored locally in a Room (SQLite) database.

---

## Build from source

```bash
# Clone the repository
git clone https://github.com/soraiyu/foxappmemo.git
cd foxappmemo

# Build a release APK (unsigned)
./gradlew :app:assembleRelease

# The APK is written to:
#   app/build/outputs/apk/release/app-release-unsigned.apk
```

### Signed release (optional)

```bash
# Generate a signing key if you don't have one
keytool -genkey -v \
  -keystore release.jks \
  -alias foxappmemo \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Sign with apksigner (part of Android build-tools)
apksigner sign \
  --ks release.jks \
  --ks-key-alias foxappmemo \
  --out app-release-signed.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## Run unit tests

```bash
./gradlew :app:test
```

---

## F-Droid metadata

Fastlane supply metadata lives in `fastlane/metadata/android/`.  
The F-Droid build recipe should point at the root of this repository.

Suggested F-Droid `metadata/com.soraiyu.foxappmemo.yml` fragment:

```yaml
Categories:
  - Utilities
License: MIT
SourceCode: https://github.com/soraiyu/foxappmemo
IssueTracker: https://github.com/soraiyu/foxappmemo/issues

Builds:
  - versionName: '1.0'
    versionCode: 1
    commit: v1.0
    subdir: app
    gradle:
      - release
```

---

## Anti-features

None declared. The app:

- requires no network access
- collects no analytics or telemetry
- stores all data on-device only
- has no ads or in-app purchases
