# Run the app from VS Code (quick guide)

## ✅ What I added for you
- `.vscode/tasks.json` with tasks to build, install, list/start emulator, and launch the app.
- `scripts/check-prereqs.ps1` to verify local tooling (JDK, adb, emulator).
- `scripts/start-emulator.ps1` helper to list or start AVDs quickly.

## 🛠 Prerequisites (install these if missing)
- Java JDK 11+ (Temurin/AdoptOpenJDK recommended)
- Android Studio (recommended — installs SDK, platform-tools, emulator, AVD manager)
- OR: Android SDK + platform-tools + emulator + add them to your PATH
- adb (part of platform-tools)

## ▶ How to use the VS Code tasks
1. Open this project root in VS Code: `File → Open Folder...` → `C:\Users\HP\FakeCallDistressApp`
2. Run the prerequisites check in an integrated terminal:
   - `pwsh -File .\\scripts\\check-prereqs.ps1`
3. Build & install on a connected device (or running emulator):
   - `Terminal → Run Task...` → select `Run: Install & Launch` (this runs `gradlew installDebug` then launches the main activity)
4. If you need to start an emulator from VS Code:
   - `Terminal → Run Task...` → `Emulator: list AVDs` then `Emulator: start AVD` (enter the AVD name)

## 🔧 If something fails
- Open Android Studio once to let it finish Gradle sync and create the `gradlew` wrapper artifacts if needed.
- Check `adb devices` to confirm a device or emulator is connected
- Make sure `app-debug.apk` exists at `app/build/outputs/apk/debug/app-debug.apk` after building

---
If you want, I can run the `check-prereqs` script in your integrated terminal now and show next missing installs. Do you want me to run it? 🤖
