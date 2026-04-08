# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-04-07

### Changed

- F-Droid metadata and fastlane supply metadata added (ja-JP locale, screenshot directories)
- versionName values are now formatted according to semantic versioning

## [1.0.0] - 2026-04-05

### Added

- Initial release of FoxAppMemo
- App management screen: add, edit, and delete records for installed apps
- Status tracking per app (e.g. using, stopped, uninstalled)
- 3-option rating system (自分向きじゃない / ふつう / 好き)
- Tag support with auto-detection from app category
- Search and filter by status, tag, and rating
- Installed apps picker: browse device apps with icons when adding a record
- App icons loaded from PackageManager displayed in lists and detail screens
- Share-intent support: receive app info shared from other apps
- Export app records as JSON via Storage Access Framework
- First-launch onboarding screen
- English and Japanese (日本語) localization (i18n)
