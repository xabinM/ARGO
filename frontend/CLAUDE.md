# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Argo is an Android educational AR (Augmented Reality) game application built with Jetpack Compose. The app combines location-based gameplay with AR visualization for cultural heritage discovery missions, designed for elementary school students and teachers.

**Key Technologies:**
- Jetpack Compose UI framework
- Hilt dependency injection
- ARCore with SceneView library
- Google Maps integration
- Retrofit for API communication
- MVVM architecture pattern

## Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Build with fastlane (requires keystore setup)
bundle exec fastlane build_apk

# Install debug build on connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.example.bogoargo.ExampleUnitTest"
```

### Lint and Quality
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug
```

## Architecture Overview

### Core Architecture
- **MVVM Pattern**: ViewModels manage UI state and business logic
- **Repository Pattern**: Data repositories abstract API and local storage
- **Dependency Injection**: Hilt manages dependencies across the app
- **Single Activity**: MainActivity hosts all Compose screens via Navigation

### Key Components

**Data Layer (`data/`)**:
- `api/`: Retrofit services with dual authentication setup (basic/authenticated)
- `repository/`: Data repositories implementing business logic
- `model/`: Data classes for domain objects
- `storage/`: Token storage with encrypted preferences
- `di/`: Hilt modules for dependency injection

**UI Layer (`ui/`)**:
- `screens/`: Compose screens organized by feature (user/, classRoom/, team/, ar/)
- `viewmodels/`: ViewModels with @HiltViewModel annotation using StateFlow
- `theme/`: Custom "Nature Theme" with child-friendly colors and components
- `navigation/`: Centralized navigation with sealed class routes

**Features**:
- **Authentication**: Login/signup with dummy data support for development
- **AR Experience**: ARCore integration with 3D model loading from assets/models/
- **Maps Integration**: Google Maps with location-based mission discovery
- **Classroom Management**: Teacher tools for creating and managing classes/teams
- **Mission System**: Location-based AR missions with progress tracking

### Navigation Structure
Navigation uses a sealed class `Screen` with typed routes supporting parameters:
- Splash → Login/SignUp → Home (role-based: Teacher vs Student)
- Game Screen → AR Screen → Mission Detail
- Teacher flows: ClassManagement, TeamManagement, ClassDetail
- User flows: Profile, Settings

### Dependency Injection Setup
**Critical**: The app uses Hilt with a dual Retrofit setup to handle authentication:
- `@Named("basic")`: For login/signup endpoints (no auth headers)
- `@Named("authenticated")`: For authenticated endpoints with token refresh

**ViewModel Requirements**: All ViewModels must use `@HiltViewModel` annotation and screens must use `hiltViewModel()` instead of `viewModel()`.

### Theme System
Custom "NatureTheme" designed for elementary students:
- Earth-tone color palette (browns, greens, yellows)
- Custom components in `NatureComponents` object
- Child-friendly UI patterns with emojis and rounded corners

### AR Implementation
- Uses `io.github.sceneview:arsceneview` library
- 3D models stored in `assets/models/` (supports .glb format)
- Location-based AR triggers with Google Maps integration
- Debug mode available for testing without physical location requirements

### Development Notes
- **Dummy Data**: Login screen includes developer buttons for testing without backend
- **API Configuration**: Backend URL in `NetworkModule.BASE_URL` (currently localhost:8080)
- **Permissions**: Requires camera, location, and AR permissions
- **Min SDK**: 33 (Android 13) due to ARCore requirements

### Common Patterns
- StateFlow for reactive UI state management
- Repository pattern with Result wrapper for API calls
- Mapper classes for DTO to domain model conversion
- Compose navigation with type-safe arguments
- Encrypted token storage for security

### Key Files for Understanding
- `AppNavigation.kt`: Central navigation configuration
- `NetworkModule.kt`: API setup with authentication handling
- `ArgoApplication.kt`: Hilt application setup
- `NatureTheme.kt`: Custom UI components and theming
- `LoginViewModel.kt`: Example of proper Hilt ViewModel setup with dummy data