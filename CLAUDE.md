# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Herrvit** (Mr. White) is an Android party game moderator app for the social deduction game "Undercover/Mr. White". Currently implements Hot Potato Mode (single-device pass-and-play). Players receive secret roles and words, then must give clues to identify the Undercover and Mr. White without revealing their own word.

## Essential Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Install on connected device/emulator
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests se.techlisbon.mrwhite.ExampleUnitTest
```

### Linting
```bash
# Run Android lint checks
./gradlew lint

# Run Kotlin lint
./gradlew lintDebug
```

## Architecture

### Single Activity Compose Application

This is a **single-module, single-activity** Android app built entirely with Jetpack Compose (no XML layouts). Navigation uses a state machine pattern with sealed classes.

**Core Navigation Flow:**
```
SetupScreen → RevealScreen (all players) → StartAnnouncement
  → Vote → Result → [MrWhiteGuess OR Repeat] → GameOver
```

### Key Components

**Screen.kt** - Sealed class defining 7 game states:
- `Setup`, `Reveal`, `StartAnnouncement`, `Vote`, `Result`, `MrWhiteGuess`, `GameOver`
- Each screen is a type-safe data class with required parameters

**MrWhiteApp.kt** - Main state machine:
- Single `mutableStateOf<Screen>` controls entire navigation
- All screen transitions happen by updating this single state variable
- No Navigation component or Fragment backstack

**GameEngine.kt** - Core game logic:
- `assignRoles()`: Distributes Civilian/Undercover/Mr. White roles based on player count
- `distributeWords()`: Randomly inverts word pairs and assigns to players
- `validateGuess()`: Checks Mr. White's final guess against both words
- Mr. White has weighted 50% chance to speak first/last

**WordLoader.kt** - Word list management:
- Fetches custom word lists from user-provided URLs using OkHttp
- Parses simple CSV format: `Word A, Word B` per line
- Falls back to 185 built-in word pairs if HTTP fails
- Uses coroutines with `Dispatchers.IO`

**RevealScreen.kt** - Anti-cheat mechanism:
- Implements touch-to-hold reveal: word only visible while finger pressed
- Uses `pointerInput { detectTapGestures }` to track press duration
- Requires 1-second hold before "Next Player" button enables
- State resets per player to prevent information leakage

**PrefsManager.kt** - Persistence:
- SharedPreferences wrapper for player names and custom word URL
- Simple comma-delimited string encoding for player list

### Material Design 3 Integration

The app uses **dynamic Material You theming** (Android 12+):
- `dynamicDarkColorScheme()` / `dynamicLightColorScheme()` extract colors from wallpaper
- Falls back to standard Material 3 theme on older devices
- All UI components use Material 3 (`androidx.compose.material3`)

### Data Models

```kotlin
data class Player(name: String, role: Role, word: String)

enum class Role { CIVILIAN, UNDERCOVER, MR_WHITE }

enum class GuessResult { CIVILIAN_WORD, UNDERCOVER_WORD, WRONG }
```

## Build Configuration

- **Namespace:** `se.techlisbon.mrwhite`
- **Min SDK:** 33 (Android 12)
- **Target SDK:** 36 (Android 15)
- **Compile SDK:** 36
- **Kotlin:** 2.2.21 with Compose Compiler Plugin
- **Compose BOM:** 2025.12.00
- **AGP:** 9.0.0-alpha09

Dependencies are centralized in `gradle/libs.versions.toml`.

## Development Notes

### Word List Format

Custom word lists must follow this format (fetched via HTTP):
```
# Comments start with #
Guitar, Violin
Harry Potter, Gandalf
Apple, Orange
```

The app randomly assigns which word becomes the Civilian word vs Undercover word.

### Testing Status

Minimal test coverage currently exists. Only scaffolding tests in:
- `app/src/test/` - JUnit 4 unit tests
- `app/src/androidTest/` - Espresso + Compose UI tests

### Roadmap Items (from README)

Future features planned:
- Firebase integration for multi-device connectivity
- Host/Join party flows with PIN/QR codes
- Text-to-speech for automated announcements
- Haptic feedback for key events
- Screen blanking for non-active players in connected mode

### Code Patterns

**State Management:**
- Uses `mutableStateOf` and `remember` for local UI state
- No ViewModel/StateFlow yet (may be needed for Firebase integration)
- State is lifted to `MrWhiteApp` for screen coordination

**Async Operations:**
- Word loading uses `LaunchedEffect` with coroutines
- OkHttp for HTTP calls (not Retrofit)
- All network calls on `Dispatchers.IO`

**UI Patterns:**
- Extensive use of `Column`, `Row`, `Card`, `Button` from Material 3
- Responsive layouts with `fillMaxWidth()` and `weight()` modifiers
- Touch gestures via `pointerInput` modifier for anti-cheat feature

## Important Files

- `app/src/main/java/se/techlisbon/mrwhite/` - All Kotlin source (18 files)
- `app/src/main/AndroidManifest.xml` - Single exported MainActivity
- `gradle/libs.versions.toml` - Dependency version catalog
- `app/build.gradle.kts` - Module-level build config
