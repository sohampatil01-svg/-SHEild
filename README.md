# SHEild: Fake Call & Distress Simulator

A comprehensive personal safety Android application designed to provide security, journey tracking, and community support for women.

## Key Features

### 1. Dashboard & SOS System
- **One-Touch SOS:** Press and hold the SOS button to instantly:
  - **Record Audio:** Captures situational audio using the microphone.
  - **Send SMS:** Dispatches an emergency SMS with your GPS location to saved contacts.
  - **Email Alert:** Opens a pre-filled email draft with your journey history and distress message.
- **Fake Call Simulator:** Trigger a realistic incoming call to help escape uncomfortable situations.
- **Shake Trigger:** Activate the fake call discreetly by shaking the device (configurable in settings).

### 2. Journey Tracking
- **Live Location Logging:** Tracks your movement in real-time using a background service.
- **Visual Feedback:** Displays recent coordinates directly on the dashboard.
- **Incident History:** Attaches your recent location history to SOS alerts for better context.

### 3. Community Wall
- **Anonymous Reporting:** A safe space to log harassment incidents or suspicious activities.
- **Local Privacy:** Incidents are stored locally on the device with timestamps.

### 4. Information Hub
- **Helpline Directory:** Instant access to critical numbers like Police (100) and Women's Helpline (1091).
- **Legal Awareness:** Summaries of key safety laws, including Zero FIR and Rights to Privacy.

### 5. Security
- **PIN Protection:** Secures the app with a personal PIN code to prevent unauthorized access.

## Automation & VS Code Integration
The project includes several automation scripts and VS Code tasks to streamline development:
- **`.vscode/tasks.json`**: Integrated tasks to build, install, and launch the app directly from VS Code.
- **`scripts/check-prereqs.ps1`**: Verifies local tooling (JDK, adb, emulator) to ensure the environment is ready.
- **`scripts/start-emulator.ps1`**: Helper script to list or start Android Virtual Devices (AVDs) quickly.

## Setup & Installation

1.  **Open in Android Studio:**
    - Load the project from the `FakeCallDistressApp` directory.
    - Allow Gradle to sync dependencies.

2.  **Permissions:**
    - On first launch, grant the required permissions:
      - **Location:** For tracking and SOS coordinates.
      - **SMS:** To send emergency alerts.
      - **Microphone:** For audio evidence recording.
      - **Notifications:** To keep the tracking service running.

3.  **Initial Configuration:**
    - **Set PIN:** Create a secure PIN on the welcome screen.
    - **Add Contact:** Go to the dashboard settings and save an Emergency Contact immediately.
    - **Test:** Use the "Fake Call" button to familiarize yourself with the interface.

## Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM Pattern
- **UI:** Material Design 3
- **Services:** Foreground Service for Location Tracking
- **Storage:** SharedPreferences & Internal Storage