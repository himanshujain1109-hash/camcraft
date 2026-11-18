# CamCraft – Advanced Android Camera Application

CamCraft is a production-quality native Android camera application built using **Kotlin**, **Jetpack Compose (Material 3)**, and **Android CameraX**. It delivers real-time camera preview, image capture, flash control, zoom, tap-to-focus, compositional grid overlays, self-timer countdowns, burst continuous capture, scoped MediaStore and Storage Access Framework (SAF) folder management, in-app photo cropping and rotation, EXIF metadata inspection, and native Android sharing.

---

## 📸 Key Features

### 1. CameraX Live Preview & Switching
- Real-time hardware preview using CameraX `PreviewView` embedded in Compose via `AndroidView`.
- Seamless switching between Front and Rear cameras with smooth transition and front-camera mirroring support.
- Compositional grid overlays: **Rule of Thirds (3×3)**, **4×4 Grid**, and **No Grid** (drawn purely over the live canvas; never burned into the captured image).
- Tap-to-focus with an animated focus indicator ring and 3-second auto-reset.
- Pinch-to-zoom and quick zoom ratio pills (1×, 2×, 5×).
- Auto, On, and Off flash modes with fallback handling for hardware lacking a dedicated LED.

### 2. High-Performance Capture & Multi-Shot Modes
- Large circular haptic-responsive shutter button.
- Transient shutter-flash animation on capture.
- Lockout protection against accidental double-captures while processing.
- **Self-Timer**: 3-second, 5-second, 10-second countdowns with real-time numeric animation, haptic ticks, and cancel option.
- **Burst Capture**: Multi-shot sequence (3, 5, or 10 frames) with progress tracking and independent file writes.
- Instant thumbnail update in the bottom bar with quick navigation to gallery.

### 3. Modern Dual-Engine Storage (MediaStore + SAF)
- **Public Pictures (Default)**: Automatically saves to `Pictures/CamCraft/` using the modern Android `MediaStore` API (`IS_PENDING` workflow on Android 10+). Instantly visible in Google Photos and system galleries without broad permissions.
- **Custom Directory (SAF)**: Select any SD card or external folder using `ACTION_OPEN_DOCUMENT_TREE` and persistent URI permissions (`takePersistableUriPermission`).
- **App-Private Storage**: Internal sandbox folder option.
- Easy folder reset button back to default storage.

### 4. Saved Photos & Gallery
- Dedicated gallery screen with a 3-column responsive grid layout.
- Sort photos by **Newest First** or **Oldest First**.
- Empty-state UI with quick shutter shortcut.
- Full-screen photo viewer with smooth multi-touch pinch-to-zoom and pan gestures.
- Delete photo with confirmation dialog.
- Native Android Sharesheet integration using `FileProvider` (`content://` URIs).

### 5. In-App Photo Editor
- **Interactive Cropping**: Freeform, 1:1 Square, 4:3, 16:9, and 3:2 aspect ratios with touch handles and dimming mask.
- **Rotation**: 90°, 180°, 270°, and one-tap reset.
- **Visual Adjustments**: Real-time Brightness, Contrast, and Saturation tuning.
- **Non-Destructive**: Saves edited photos as a new file (`CamCraft_Edit_<timestamp>.jpg`), leaving the original photo untouched.

### 6. EXIF Metadata & Geotagging
- Complete metadata viewer: capture timestamp, resolution (width × height), camera make & model, ISO sensitivity, aperture, exposure time, and GPS coordinates.
- **Optional GPS Geotagging**: Tag photos with latitude and longitude only when explicitly enabled by the user with runtime location permissions. No location data is collected or transmitted.

### 7. Modern UI / UX & Architecture
- **Design System**: Material 3 with OLED deep charcoal dark theme and Electric Blue / Neon Orange accents.
- **Architecture**: MVVM with unidirectional data flow (UDF), `ViewModel`, and `StateFlow`.
- **Concurrency**: Kotlin Coroutines + `Dispatchers.IO` / `Dispatchers.Default` for all image I/O and processing.

---

## 🏗️ Architecture & Package Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/camcraft/
│   ├── CamCraftApplication.kt           # Application class & Coil cache configuration
│   ├── MainActivity.kt                  # Single Activity, Theme host & edge-to-edge
│   ├── camera/
│   │   ├── CameraManager.kt             # CameraX binding, capture, preview, focus & zoom
│   │   └── CameraState.kt               # Reactive UI state for camera controls
│   ├── data/
│   │   └── Models.kt                    # Data classes & Enums (CapturedPhoto, FlashMode, etc.)
│   ├── editor/
│   │   ├── CropAspectRatio.kt           # Preset crop aspect ratios
│   │   └── ImageEditor.kt               # Background bitmap transforms & save
│   ├── metadata/
│   │   ├── ExifManager.kt               # AndroidX ExifInterface read/write utilities
│   │   └── PhotoMetadata.kt             # Parsed metadata representation
│   ├── navigation/
│   │   ├── NavGraph.kt                  # Animated Jetpack Navigation graph
│   │   └── Screen.kt                    # Type-safe navigation routes
│   ├── permissions/
│   │   └── PermissionManager.kt         # Runtime permissions & app settings redirection
│   ├── settings/
│   │   ├── AppSettings.kt               # Settings data model
│   │   └── SettingsManager.kt           # Persistent preferences & SAF permissions
│   ├── storage/
│   │   ├── AppStorageManager.kt         # Private storage provider
│   │   ├── MediaStoreManager.kt         # Public MediaStore Pictures provider
│   │   ├── PhotoRepository.kt           # Unified photo coordinator
│   │   └── SafStorageManager.kt         # Storage Access Framework provider
│   ├── ui/
│   │   ├── components/
│   │   │   ├── BurstProgressOverlay.kt  # Burst capture overlay
│   │   │   ├── CameraControls.kt        # Top & bottom camera toolbars & shutter
│   │   │   ├── CameraPreview.kt         # CameraX PreviewView with gestures
│   │   │   ├── CropImageView.kt         # Custom interactive crop canvas
│   │   │   ├── FocusIndicator.kt        # Tap-to-focus animated ring
│   │   │   ├── GridOverlay.kt           # 3x3 and 4x4 rule of thirds canvas
│   │   │   ├── PermissionDialog.kt      # Material 3 rationale dialog
│   │   │   ├── TimerOverlay.kt          # Large countdown animation
│   │   │   └── ZoomControls.kt          # Zoom pills & controls
│   │   ├── screens/
│   │   │   ├── CameraScreen.kt          # Camera viewfinder & controls
│   │   │   ├── GalleryScreen.kt         # Photo grid & sorting
│   │   │   ├── HomeScreen.kt            # Landing dashboard & hardware status
│   │   │   ├── PhotoEditorScreen.kt     # Crop, rotate & color adjustments
│   │   │   ├── PhotoViewerScreen.kt     # High-res zoom, share, delete & EXIF
│   │   │   └── SettingsScreen.kt        # Storage, defaults & theme preferences
│   │   └── theme/
│   │       ├── Color.kt                 # Electric Blue & Charcoal palette
│   │       ├── Theme.kt                 # Material 3 Dynamic Theme
│   │       └── Type.kt                  # Typography definitions
│   └── utils/
│       ├── BitmapUtils.kt               # Bitmap decoding, matrix transform & compression
│       ├── HapticFeedbackHelper.kt      # Haptic feedback for shutter, ticks, burst
│       └── LocationHelper.kt            # GPS location provider for geotagging
└── res/
    ├── drawable/                        # Vector assets & icons
    ├── mipmap-anydpi-v26/               # Adaptive launcher icons
    ├── values/                          # Colors, strings, themes
    └── xml/file_paths.xml               # FileProvider configuration
```

---

## 🛠️ Build & Installation Instructions

### Requirements
- Android Studio Ladybug (2024.2+) or newer.
- Android SDK Platform 35.
- JDK 17 or JDK 21.
- Minimum supported Android version: Android 8.0 (API 26).
- Target Android version: Android 15 (API 35).

### Build with Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the `CamCraft` directory (`C:\Users\Dell\.gemini\antigravity\scratch\CamCraft`).
3. Allow Gradle to synchronize dependencies.
4. Select a connected physical Android device or emulator running API 26+.
5. Click **Run** (`Shift + F10`) or **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Build via Command Line
Run the following command from the project root:

```bash
# On Windows PowerShell:
.\gradlew assembleDebug

# On macOS / Linux:
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Permission & Privacy Design
- **Camera (`CAMERA`)**: Requested only when opening the camera preview. If permanently denied, directs the user gracefully to system App Settings.
- **Location (`ACCESS_FINE_LOCATION`)**: Only requested when the user activates "GPS Geotagging" in Settings. Location data is strictly written to the photo's local EXIF block and is never uploaded or shared.
- **Storage**: Adheres strictly to modern Android storage policies. Uses `MediaStore` (Scoped Storage) for public pictures and the Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`) for user-designated folders. No broad `MANAGE_EXTERNAL_STORAGE` permissions are requested.
