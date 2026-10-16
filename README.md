# Offline Fitness Tracker

Fast, privacy-focused, 100% offline native Android fitness and workout tracker built with **Kotlin**, **Jetpack Compose**, and **Room Database**.

[![Android Build CI](https://github.com/actions/workflows/build.yml/badge.svg)](#)
![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-brightgreen)
![Tech Stack](https://img.shields.io/badge/Stack-Kotlin%20%7C%20Compose%20%7C%20Room-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Features

- **100% Offline & Private**: Zero registration, zero cloud accounts, and zero remote servers. All data is kept locally on your phone in an encrypted-at-rest SQLite database.
- **Zero Tracking**: Does not declare `INTERNET` permission in `AndroidManifest.xml`. No telemetry, analytics, or third-party ads.
- **Active Workout Logger**:
  - Live elapsed workout timer that persists across app backgrounding and screen locks.
  - Multi-set types: Normal (`N`), Warm-up (`W`), Drop Set (`D`), To Failure (`F`), and Timed (`T`).
  - Automatic rest timer with quick increments (`+15s`, `+30s`) and optional haptic vibration notification.
  - Interruption & crash recovery with one-tap **Resume Workout** banner.
- **Exercise Library**:
  - 23+ pre-populated exercises across 9 muscle groups (Chest, Back, Shoulders, Arms, Legs, Glutes, Core, Cardio, Full Body).
  - Create and archive custom exercises with equipment tags and form cues.
- **Reusable Routines & Templates**: Pre-configured workout splits and custom routine builder with target sets and repetitions.
- **Personal Records & Analytics**:
  - Automatically identifies all-time heaviest lifts and repetition personal records.
  - One-Repetition Maximum (1RM) estimations calculated via the validated **Epley formula**.
  - Canvas-rendered volume progression chart.
- **Body Measurements Log**: Track body weight, waist, chest, arms, thighs, and hips offline.
- **Unit System**: Full metric (`kg`, `cm`) and imperial (`lbs`, `in`) toggle without loss of stored data precision.
- **Data Export & Import**:
  - Complete JSON backup export and import with schema validation preview.
  - Tabular CSV export for workouts and body measurements.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material 3)
- **Local Persistence**: Room Database (KSP-generated DAOs)
- **Preferences**: Jetpack DataStore (Preferences)
- **Architecture**: MVVM with Repository Pattern, StateFlow, and Coroutines
- **Navigation**: Jetpack Navigation Compose
- **Testing**: JUnit 4, Robolectric, Coroutines Test

---

## Building from Source

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 36

### Build Commands
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/offline-fitness-tracker.git
cd offline-fitness-tracker

# Run unit and database tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Build Android App Bundle (AAB) for Google Play
./gradlew bundleDebug
```

Built artifacts will be generated in:
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- AAB: `app/build/outputs/bundle/debug/app-debug.aab`
