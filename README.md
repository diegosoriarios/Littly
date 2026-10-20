# Littly - Habit Tracker with AI-Powered Suggestions

Littly is a native Android habit tracker that helps users build consistent habits through gentle reminders and AI-powered suggestions. The app uses an on-device language model to provide personalized, contextual ideas for each habit.

## Features

### Core Functionality
- **Habit Management**: Create, edit, and track daily habits with customizable goal days
- **Streak Tracking**: Visual streak tracking with goal day awareness
- **Smart Reminders**: Configurable notifications with exact alarm support
- **Habit Archiving**: Archive completed or inactive habits

### AI-Powered Features
- **On-Device LLM**: Uses Gemma 3 1B model for privacy-focused AI suggestions
- **Contextual Ideas**: Get personalized suggestions based on habit category
- **Smart Classification**: Automatic habit categorization (creative, reading, learning, fitness, mindfulness, social, productivity)
- **Anti-Repeat Logic**: Avoids suggesting the same ideas repeatedly

### User Experience
- **Material Design 3**: Modern, adaptive UI with dynamic colors
- **Dark Mode**: Full dark theme support
- **Home Screen Widget**: Quick habit completion from the home screen
- **Battery Optimization**: Smart background processing with WorkManager

## Architecture

### Tech Stack
- **Language**: Kotlin 2.2.10
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room for local persistence
- **DI**: Hilt for dependency injection
- **Background Processing**: WorkManager for scheduled tasks
- **Notifications**: AlarmManager for precise reminders
- **LLM Integration**: MediaPipe for on-device inference

### Project Structure
```
app/src/main/java/com/diego/littly/
├── core/
│   ├── common/          # Shared utilities and base classes
│   ├── data/            # Repository implementations
│   ├── database/        # Room entities, DAOs, and database
│   ├── designsystem/    # Theme, colors, typography, components
│   ├── domain/          # Business logic and use cases
│   ├── download/        # Model download management
│   ├── llm/             # LLM integration and idea generation
│   ├── navigation/      # Deep linking and navigation state
│   └── notifications/   # Notification and alarm scheduling
├── feature/
│   ├── habits/          # Habit list, detail, and edit screens
│   ├── onboarding/      # First-run onboarding flow
│   └── settings/        # App settings screen
└── widget/              # Home screen widget implementation
```

### Key Components

#### Data Layer
- **Room Database**: Stores habits, completion logs, and AI-generated ideas
- **Repositories**: Abstract data access with Flow-based reactive streams
- **Migration Strategy**: Schema versioning with Room migrations

#### Domain Layer
- **Use Cases**: Business logic for habit tracking and idea generation
- **Streak Calculator**: Intelligent streak calculation respecting goal days
- **Idea Queue Filler**: Pre-generates ideas for upcoming reminders

#### Presentation Layer
- **ViewModels**: State management with StateFlow
- **Compose Screens**: Declarative UI with reactive state
- **Navigation**: Type-safe navigation with Compose Navigation

#### LLM Integration
- **IdeaGenerator Interface**: Abstraction for different LLM providers
- **MediaPipe Implementation**: On-device inference with Gemma 3 1B
- **Fallback Strategy**: NoopIdeaGenerator for devices without LLM support
- **JSON Parser**: Robust parsing with multiple recovery strategies

#### Widget
- **Glance Framework**: Modern widget implementation
- **Hilt Integration**: Dependency injection in widget context
- **Real-time Updates**: Automatic refresh on data changes

## Setup

### Prerequisites
- Android Studio Ladybug or later
- JDK 21 (Android Studio bundled JBR)
- Android SDK 36
- Minimum SDK 24 (Android 7.0)

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd Littly

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# Install on connected device
./gradlew installDebug
```

### Model Download
The app requires downloading the Gemma 3 1B model (~550MB) on first launch:
- Downloads automatically when Wi-Fi is available
- Can be triggered manually from Settings
- Supports resume if interrupted
- Stored in app's internal storage

## Permissions

The app requests the following permissions:
- `POST_NOTIFICATIONS` (Android 13+): For habit reminders
- `SCHEDULE_EXACT_ALARM` (Android 12+): For precise reminder timing
- `RECEIVE_BOOT_COMPLETED`: To reschedule alarms after device restart
- `FOREGROUND_SERVICE`: For model download progress
- `INTERNET`: For downloading the LLM model

## Testing

The project includes comprehensive unit tests:
- **86 unit tests** covering business logic
- **Streak calculation** with goal day awareness
- **Repository operations** with in-memory Room database
- **LLM integration** with mock generators
- **Notification scheduling** logic

Run tests:
```bash
./gradlew testDebugUnitTest
```

## Development Milestones

The project was developed incrementally through 8 milestones:

1. **M0 - Project Foundation**: Gradle setup, theme, app shell
2. **M1 - Data Layer**: Room database, repositories, streak calculation
3. **M2 - Habit CRUD**: List, detail, and edit screens
4. **M3 - Notifications**: Alarms, receivers, permission handling
5. **M4 - LLM Layer**: IdeaGenerator, MediaPipe integration, classifiers
6. **M5 - Model Download**: WorkManager-based download with resume
7. **M6 - Idea Queue**: Pre-generation and notification integration
8. **M7 - Widget**: Glance home screen widget

## Known Limitations

- Model download requires ~550MB storage
- LLM inference may be slow on older devices
- Exact alarm permission may be revoked by system on some devices
- Widget updates are limited by Android system constraints

## Future Enhancements

- Cloud sync for habit data
- Multiple LLM model support
- Habit templates and suggestions
- Social features and accountability
- Advanced analytics and insights

## License

This project is proprietary software.

## Contact

For questions or feedback, please open an issue on the repository.
