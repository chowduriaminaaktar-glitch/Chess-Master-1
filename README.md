# Chess Duel ♔

A tournament-grade two-player chess game built with Kotlin and Jetpack Compose, specifically designed for pass-and-play matches on a single Android device.

## ✨ Features

- **One-Mobile Pass & Play**:
  - **Face-to-Face Mode**: Designed for two players sitting across each other with the phone flat on a table. The top player's clock, player badge, and captured pieces automatically face them (180° rotation).
  - **Auto-Flip Mode**: Automatically rotates the board on each turn so the active player always views from their perspective.
  - **Pass Turn Shield**: Optional privacy curtain when handing the phone between players.
- **Dual Digital Chess Clocks**: Supports Casual (No timer), Bullet (1|0, 2|1), Blitz (3|0, 3|2, 5|0), and Rapid (10|0, 15|10) with low-time audio warnings.
- **Realistic UI & Themes**: Custom vector-drawn Staunton chess pieces (ivory & obsidian), 3 board themes (*Tournament Walnut*, *Midnight Slate*, *Emerald Master*), move history sheet, and tactile feedback.
- **Complete Rules Engine**: Castling, En Passant, Pawn Promotion, Check/Checkmate, Stalemate, 50-move rule, and Insufficient Material draw.
- **Local Match History & Stats**: Room database storing completed games with PGN notation and win rate analytics.

---

## 📱 How to Download and Install the APK

### Method 1: Download from GitHub Actions (Automatic on every push)

Every commit to `main`/`master` automatically builds a fresh installable APK.

1. Open your repository on GitHub.
2. Click the **Actions** tab at the top.
3. Click on the latest workflow run named **"Build Android APK"**.
4. Scroll down to the **Artifacts** section at the bottom of the summary page.
5. Click **App-APK** to download the ZIP file.
6. Extract the ZIP file to get `ChessDuel-debug.apk`.
7. Transfer the APK to your Android device (or download directly on your phone) and open it to install.

> **Note**: Android will prompt to allow "Install from unknown sources" for your browser or file manager. Enable it to complete installation.

---

### Method 2: Download from GitHub Releases

To create a tagged release with the APK permanently attached:

1. Push a version tag to GitHub:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. The GitHub Actions workflow will automatically build the APK and publish a release.
3. Go to the **Releases** tab on GitHub (on the right sidebar).
4. Download `ChessDuel-debug.apk` directly from the release assets without needing to unzip.

Alternatively, you can manually trigger a release from GitHub:
1. Go to **Actions** -> **Build Android APK**.
2. Click **Run workflow**.
3. Check the box **"Publish a GitHub Release with this APK"**.
4. Click **Run workflow**.

---

## 🛠️ Building Locally

### Prerequisites
- **JDK 21** (Temurin or OpenJDK)
- **Android SDK** (API Level 36)

### Build Commands

Using the Gradle wrapper:

```bash
# Ensure execution permissions on Unix/macOS:
chmod +x gradlew

# Build the debug APK:
./gradlew :app:assembleDebug

# Run unit tests:
./gradlew :app:testDebugUnitTest
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install directly to a connected device via ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
