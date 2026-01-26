# 🎵 SonicFlow

> A modern, feature-rich local audio player for Android with an elegant violet-themed dark UI

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## ✨ Features

### 🎶 Audio Playback
- **Background Playback**: Play music seamlessly even when the app is in the background using Media3's `MediaSessionService`
- **Media Controls**: Full control over playback with play, pause, skip, shuffle, and repeat modes
- **Waveform Visualization**: Beautiful real-time audio waveform display using Canvas API
- **Seek & Scrub**: Precise control over playback position with an intuitive seek bar

### 📚 Library Management
- **Auto-Scan**: Automatically discover and catalog all audio files on your device using MediaStore API
- **Smart Organization**: Browse by tracks, albums, and smart lists
- **Search**: Quickly find tracks by title, artist, or album
- **Sort Options**: Organize your library by title, artist, or date added

### 🎵 Playlist Management
- **Create Playlists**: Build custom playlists for any mood or occasion
- **Drag & Drop**: Reorder tracks in playlists with intuitive drag-and-drop
- **Quick Actions**: Add tracks to playlists directly from the library
- **Smart Management**: Track counts and metadata automatically updated

### 🎨 Modern UI/UX
- **Dark Theme**: Eye-friendly dark theme with vibrant violet/purple accents
- **Material Design 3**: Latest Material Design guidelines for a polished experience
- **Smooth Animations**: Fluid transitions and interactions throughout the app
- **Edge-to-Edge**: Modern Android design with immersive display

## 🛠️ Tech Stack

### Architecture & Patterns
- **Clean Architecture**: Separation of concerns with data, domain, and presentation layers
- **MVVM Pattern**: Model-View-ViewModel for reactive UI updates
- **Repository Pattern**: Abstraction layer for data sources
- **Use Cases**: Single responsibility business logic components

### Core Technologies
| Technology | Purpose |
|------------|---------|
| **Kotlin** | Modern, concise, and safe programming language |
| **Jetpack Compose** | Declarative UI framework for building native Android UI |
| **Hilt** | Dependency injection for managing object creation |
| **Room** | Local database for persistent storage of tracks and playlists |
| **Media3** | Modern media playback with MediaSession support |
| **Navigation Compose** | Type-safe navigation between screens |
| **DataStore** | Key-value storage for app preferences |
| **Kotlin Coroutines** | Asynchronous programming with structured concurrency |
| **Flow** | Reactive streams for data observation |

### Key Libraries
```kotlin
// UI
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui")
implementation("io.coil-kt:coil-compose:2.6.0")

// Architecture
implementation("com.google.dagger:hilt-android:2.51.1")
implementation("androidx.navigation:navigation-compose:2.7.7")

// Data
implementation("androidx.room:room-ktx:2.6.1")
implementation("androidx.datastore:datastore-preferences:1.1.1")

// Media
implementation("androidx.media3:media3-exoplayer:1.4.0")
implementation("androidx.media3:media3-session:1.4.0")
```

## 📁 Project Structure

```
app/src/main/java/com/sonicflow/
├── data/                           # Data Layer
│   ├── local/
│   │   ├── db/
│   │   │   ├── SonicFlowDatabase.kt      # Room database
│   │   │   ├── dao/
│   │   │   │   ├── TrackDao.kt           # Track data access
│   │   │   │   └── PlaylistDao.kt        # Playlist data access
│   │   │   └── entity/
│   │   │       ├── TrackEntity.kt        # Track database model
│   │   │       ├── PlaylistEntity.kt     # Playlist database model
│   │   │       ├── PlaylistTrackCrossRef.kt  # Many-to-many relation
│   │   │       └── WaveformEntity.kt     # Waveform data model
│   │   ├── PlaybackPreferences.kt        # DataStore preferences
│   └── repository/
│       ├── TrackRepository.kt            # Track data repository
│       └── PlaylistRepository.kt         # Playlist data repository
│
├── domain/                         # Domain Layer
│   ├── model/
│   │   ├── Track.kt                      # Track domain model
│   │   └── Playlist.kt                   # Playlist domain model
│   └── usecase/
│       ├── GetTracksUseCase.kt           # Retrieve tracks use case
│       └── ManagePlaylistUseCase.kt      # Manage playlists use case
│
├── di/                             # Dependency Injection
│   └── AppModule.kt                      # Hilt module
│
├── media/                          # Media Layer
│   ├── PlaybackService.kt                # MediaSessionService
│   ├── MediaControllerManager.kt         # Media control manager
│   └── WaveformExtractor.kt              # Audio waveform extraction
│
├── ui/                             # Presentation Layer
│   ├── theme/
│   │   ├── Color.kt                      # App color palette
│   │   ├── Theme.kt                      # Material3 theme
│   │   └── Type.kt                       # Typography definitions
│   ├── navigation/
│   │   ├── Screen.kt                     # Screen destinations
│   │   └── SonicFlowNavGraph.kt          # Navigation graph
│   ├── auth/
│   │   ├── SignInScreen.kt               # Sign in screen
│   │   ├── SignUpScreen.kt               # Sign up screen
│   │   └── AuthViewModel.kt              # Auth view model
│   ├── library/
│   │   ├── LibraryScreen.kt              # Library screen
│   │   ├── LibraryViewModel.kt           # Library view model
│   │   └── components/
│   │       ├── TrackItem.kt              # Track list item
│   │       └── SearchBar.kt              # Search bar component
│   ├── player/
│   │   ├── PlayerScreen.kt               # Player screen
│   │   ├── PlayerViewModel.kt            # Player view model
│   │   └── components/
│   │       ├── WaveformView.kt           # Waveform visualization
│   │       ├── PlayerControls.kt         # Playback controls
│   │       └── SeekBar.kt                # Seek bar component
│   └── playlist/
│       ├── PlaylistScreen.kt             # Playlist list screen
│       ├── PlaylistDetailScreen.kt       # Playlist detail screen
│       └── PlaylistViewModel.kt          # Playlist view model
│
├── SonicFlowApplication.kt         # Application class
└── MainActivity.kt                 # Main activity
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 11 or higher
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

### Build & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/fiderana30215/SonicFlow.git
   cd SonicFlow
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned repository and open it

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for the sync to complete

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift + F10`

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 🎨 Design Philosophy

SonicFlow embraces a **dark-first design** with vibrant violet accents, providing:

- **Visual Comfort**: Easy on the eyes during extended listening sessions
- **Energy Efficiency**: OLED-friendly dark backgrounds reduce battery consumption
- **Focus on Content**: Album art and waveforms pop against the dark background
- **Modern Aesthetics**: Material Design 3 with custom theming

### Color Palette
- **Primary**: `#8B5CF6` (Vibrant Violet)
- **Secondary**: `#A78BFA` (Lighter Violet)
- **Tertiary**: `#6D28D9` (Deeper Violet)
- **Background**: `#121212` (Pure Dark)
- **Surface**: `#1E1E1E` (Elevated Surface)

## 🔒 Permissions

SonicFlow requires the following permissions:

```xml
<!-- Audio file access -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />

<!-- Background playback -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Keep device awake during playback -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 📋 Development Roadmap

- [x] Project setup and architecture
- [x] Database layer with Room
- [x] MediaStore integration
- [x] Audio playback with Media3
- [x] Waveform visualization
- [x] Library browsing and search
- [x] Playlist management
- [ ] Equalizer support
- [ ] Lyrics display
- [ ] Cloud sync (optional)
- [ ] Sleep timer
- [ ] Crossfade between tracks
- [ ] Tag editing
- [ ] Themes customization

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Fiderana**
- GitHub: [@fiderana30215](https://github.com/fiderana30215)

## 🙏 Acknowledgments

- **Material Design** for design guidelines
- **Media3** team for the excellent media playback library
- **Jetpack Compose** team for the modern UI toolkit
- **Android Community** for continuous support and resources

---

<p align="center">Made with ❤️ and 🎵 by Fiderana</p>
