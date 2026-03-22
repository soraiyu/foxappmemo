# FoxAppMemo

A fully offline Android app for tracking, rating, and annotating your installed apps.

---

## Features

- **Browse installed apps** – pick any app from the on-device list and add it to your memo list with one tap
- **Rich memos** – attach a free-text note to any app
- **Simple ratings** – three-level rating: 自分向きじゃない / ふつう / 好き
- **Status labels** – _Trying / Main / Avoid / Blacklist / Reconsider_
- **Tags** – create tags on the fly and filter by multiple tags at once; app genre is auto-detected from the OS and pre-filled as a tag
- **Powerful filters** – search by name or package name, filter by status, tags, and specific rating values via chips
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

### Option A — GitHub Actions (recommended, no local setup needed)

The repository includes a CI workflow (`.github/workflows/build.yml`) that builds
a debug APK automatically on every push and pull request.

#### 1. Enable GitHub Actions for this repository

> Required **only once** by the repository owner.

1. Open the repository on GitHub.
2. Go to **Settings → Actions → General**.
3. Under **Actions permissions**, select **Allow all actions and reusable workflows**.
4. Under **Workflow permissions**, select **Read and write permissions**.
5. Click **Save**.

#### 2. Approve the first workflow run (if prompted)

If you see a yellow banner saying _"This workflow requires approval"_:

1. Click the **[Actions tab](../../actions)**.
2. Find the run named **"Build APK"** with status _"Action required"_.
3. Click **Review pending deployments** (or open the run and click **Approve and run**).

#### 3. Download the APK

Once the workflow finishes (green check-mark ✅):

1. Click the **[Actions tab](../../actions)** and open the latest **"Build APK"** run.
2. Scroll to the **Artifacts** section at the bottom.
3. Click **app-debug** to download the ZIP — it contains `app-debug.apk`.

You can also trigger a build at any time from **Actions → Build APK → Run workflow**.

---

### Option B — Build locally

#### Requirements

| Tool | Version |
|------|---------|
| JDK | 17 |
| Android SDK | Platform 35, Build-Tools 35.x |

```bash
git clone https://github.com/soraiyu/foxappmemo.git
cd foxappmemo
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

Release build (unsigned):

```bash
./gradlew assembleRelease
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
