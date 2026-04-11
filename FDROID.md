# F-Droid Build Notes

This document describes how to reproduce a reproducible / F-Droid-compatible build of **FoxAppMemo** and how to submit it to F-Droid.

---

## Why F-Droid submission makes sense

FoxAppMemo is a strong candidate for F-Droid because it meets all core requirements:

| Requirement | Status |
|---|---|
| Free and Open Source (MIT) | ✅ |
| No proprietary SDKs | ✅ (AndroidX / Jetpack only) |
| No Firebase / Play Services / Analytics | ✅ |
| No INTERNET permission | ✅ |
| No ads or in-app purchases | ✅ |
| Reproducible build from source | ✅ |
| Fastlane metadata present | ✅ |

---

## How to submit to F-Droid

1. **Open a Request for Packaging (RFP)** on the F-Droid issue tracker:  
   <https://gitlab.com/fdroid/rfp/-/issues/new>  
   Use the provided template and include the source URL and a brief description.

2. **Wait for a packager** to pick up the request. You can also write the build recipe yourself and open a merge request against [fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata).

3. **The build recipe** (`metadata/com.rtneg.foxappmemo.yml`) for fdroiddata should look like the fragment in the [F-Droid metadata](#f-droid-metadata) section below.

   **Note:** The source code package was renamed from `com.soraiyu.foxappmemo` to `com.rtneg.foxappmemo`, but the Android `applicationId` (F-Droid/device package identifier) intentionally remains `com.soraiyu.foxappmemo` to preserve upgrade continuity for existing users. Existing users will receive this as a normal update rather than a new app installation.

4. **Add screenshots** to `fastlane/metadata/android/en-US/images/phoneScreenshots/` (and `ja-JP/images/phoneScreenshots/`) in the repository. F-Droid will pick them up automatically via the fastlane supply format. Without screenshots the store listing will appear bare.

5. **Add an app icon** at `fastlane/metadata/android/en-US/images/icon.png` (512 × 512 px) for the F-Droid listing.

---

## Locales included in fastlane metadata

| Locale | Path |
|---|---|
| English (en-US) | `fastlane/metadata/android/en-US/` |
| Japanese (ja-JP) | `fastlane/metadata/android/ja-JP/` |

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

Suggested F-Droid `metadata/com.rtneg.foxappmemo.yml` fragment:

```yaml
Categories:
  - Utilities
License: MIT
SourceCode: https://github.com/soraiyu/foxappmemo
IssueTracker: https://github.com/soraiyu/foxappmemo/issues

Builds:
  - versionName: '1.1.0'
    versionCode: 2
    commit: v1.1.0
    subdir: app
    gradle:
      - yes
```

---

## Anti-features

None declared. The app:

- requires no network access
- collects no analytics or telemetry
- stores all data on-device only
- has no ads or in-app purchases
