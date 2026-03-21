# FoxAppMemo

A fully offline Android app for tracking, rating, and annotating your installed apps.

---

## Features

- **Browse installed apps** – pick any app from the on-device list and add it to your memo list with one tap
- **Rich memos** – attach a free-text note to any app
- **Star ratings** – 1–5 stars with an interactive rating bar
- **Status labels** – _Trying / Main / Avoid / Blacklist / Reconsider_
- **Tags** – create tags on the fly and filter by multiple tags at once
- **Powerful filters** – search by name or package name, filter by status, tags, and minimum rating
- **JSON export** – save your full memo list as a pretty-printed JSON file using the Storage Access Framework (you choose the destination)
- **Swipe to delete** with confirmation
- **Material You / Material 3** design with a warm fox-inspired colour palette

---

## Privacy

- **No internet permission** – the app never makes any network requests
- All data is stored locally in a Room (SQLite) database on your device
- The only permission declared is `QUERY_ALL_PACKAGES` (required on Android 11+ to enumerate installed apps)

---

## Build from source

### Requirements

| Tool | Version |
|------|---------|
| JDK | 17 |
| Android SDK | Platform 35, Build-Tools 35.x |

```bash
git clone https://github.com/soraiyu/foxappmemo.git
cd foxappmemo
./gradlew :app:assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Run tests

```bash
./gradlew :app:test
```

See [FDROID.md](FDROID.md) for signing instructions and the F-Droid build recipe.

---

## Tech stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Database | Room |
| DI | Hilt |
| Serialization | Kotlinx Serialization |
| Async | Kotlin Coroutines / Flow |

---

## License

MIT
