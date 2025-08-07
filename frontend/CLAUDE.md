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
- **Clean Architecture**: Clear separation between data, domain, and UI layers
- **MVVM Pattern**: ViewModels manage UI state, UseCases handle business logic
- **Repository Pattern**: Data repositories abstract API and local storage
- **Dependency Injection**: Hilt manages dependencies across the app
- **Single Activity**: MainActivity hosts all Compose screens via Navigation

### Key Components

**Data Layer (`data/`)**:
- `api/`: Retrofit services (AuthApiService, ClassApiService, TeamApiService, UserApiService, ApplicationApiService)
- `repository/`: Repository implementations (AuthRepository, ClassRepository, TeamRepository, etc.)
- `dto/`: Request/Response DTOs organized by feature
- `mapper/`: Mapper classes converting DTOs to domain models
- `storage/`: TokenStorage with encrypted SharedPreferences
- `cache/`: In-memory caching (MissionCache)
- `preferences/`: PreferencesManager for app settings

**Domain Layer (`domain/`)**:
- `model/`: Domain models (User, Class, Team, Mission, AR3DObject, etc.)
- `repository/`: Repository interfaces (IAuthRepository, IClassRepository, etc.)
- `use_case/`: Business logic use cases organized by feature (auth/, classroom/, team/, mission/, etc.)

**UI Layer (`ui/`)**:
- `screens/`: Compose screens organized by feature (user/, classRoom/, team/, ar/)
- `viewmodels/`: ViewModels with @HiltViewModel annotation using StateFlow
- `theme/`: Custom "Nature Theme" with child-friendly colors and components
- `components/`: Reusable UI components

**Dependency Injection (`di/`)**:
- `NetworkModule.kt`: Retrofit setup with dual authentication
- `RepositoryModule.kt`: Repository bindings
- `UseCaseModule.kt`: UseCase dependencies
- `ViewModelModule.kt`: ViewModel dependencies

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
- **Target SDK**: 36 with Compile SDK 36
- **Java Version**: 11 for both source and target compatibility

### Common Patterns
- **UseCase Pattern**: Business logic encapsulated in individual use cases
- **StateFlow**: Reactive UI state management with StateFlow/Flow
- **Repository Pattern**: Data abstraction with Result wrapper for API calls
- **Mapper Pattern**: DTO to domain model conversion with dedicated mapper classes
- **Compose Navigation**: Type-safe navigation with sealed class routes
- **Encrypted Storage**: TokenStorage using Android Security library
- **Dual Network Setup**: Separate Retrofit instances for authenticated/unauthenticated requests

### Key Files for Understanding
- `AppNavigation.kt`: Central navigation configuration with sealed class routes
- `NetworkModule.kt`: Dual Retrofit setup with TokenManagementInterceptor
- `ArgoApplication.kt`: Hilt application entry point
- `NatureTheme.kt`: Custom UI components and child-friendly theming
- `LoginViewModel.kt`: Example ViewModel with UseCase integration and dummy data
- `TokenStorage.kt`: Encrypted token management with Android Security
- `*UseCase.kt` files: Business logic layer between ViewModels and Repositories