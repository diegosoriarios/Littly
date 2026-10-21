# Littly

**Little by little.**

Littly is a native Android habit tracker designed to feel warm, calm, and encouraging. It works entirely offline for habit tracking and reminders. Optional idea suggestions can run through an on-device model, with a curated built-in fallback when the model is unavailable.

## What is implemented

### Today
- Date, time-aware greeting, optional display name, avatar, and reminder shortcut.
- Navy summary card with the best current habit streak, today’s completion ring, and a real seven-day strip.
- Habits scheduled for today with streak badges, recent activity dots, and accessible completion controls.
- Saved idea card with truthfully labeled source (`On-device` or `Built-in`), open-habit action, and “Another idea” deep link.
- Empty, rest-day, all-done, loading, and error states.
- Floating add-habit action.

### Habits
- Active and paused counts, large page title, and all-habit reset pill.
- Reminder-time filters: All day, Morning, Afternoon, Evening, and Paused.
- Responsive two-column grid that drops to one column on very narrow screens or large font scales.
- Stable accent and icon per habit, real current streak, recent activity dots, and a navy “best” card.
- Paused habits can be restored. Long names wrap without clipping.
- Dashed new-habit card and floating add action.

### Habit Detail
- Back, edit, archive, and restore actions with confirmation.
- Navy summary card with habit identity, schedule, current streak, all-time best, and real 30-day completion rate.
- Three-month binary heatmap. A habit only stores completed/not-completed data, so intensity levels are deliberately not implied.
- Idea card with generation states, source labels, recent ideas, and a disabled state when ideas are turned off.
- Prominent completion button and an accurate offline/fallback explanation.

### Stats
- Month selector with previous/next navigation and future months disabled.
- Monthly calendar heatmap where intensity expresses the real fraction of due habits completed that day.
- Completion ring, actual month-over-month change when available, and total check-ins since the first stored completion.
- Six-month completion trend drawn from stored data.
- All-time best streak with the habit name and date range.
- New-user and insufficient-history states.

### Add / Edit Habit
- Free-text name and optional description.
- Stable icon and accent-color pickers.
- Goal-day selection.
- Optional reminder with time and independent reminder days.
- Inline validation and helpful feedback.
- Notification permission is requested only in context, when reminders are enabled.
- Editing preserves completion history, creation date, sort order, and archive state.

### Onboarding
- Four branded pages: optional name, reminders, background reliability, and optional ideas model.
- Optional display name saved on-device and editable later in Profile.
- Fully skippable. No unexpected notification request on launch.
- Model download is clearly optional and can be skipped, cancelled, retried, or completed.

### Profile / Settings
- Optional display name.
- System / Light / Dark theme preference.
- Reminder master switch, notification permission status, exact-alarm status, and battery-optimization guidance.
- Idea-engine toggle with model status, download progress, cancel, retry, and delete.
- Privacy information: habits, check-ins, and ideas stay in a local database; there is no account or analytics; the network is used only for an optional model download.

### Widgets
- **Overview:** responsive list with real completion state, best current streak, quick tap-to-complete, and graceful empty states. If nothing is scheduled for today, it falls back to all active habits instead of showing an empty card.
- **Streak:** best current habit streak with the habit name; opens that habit.
- **Idea:** latest saved idea for an active habit, or honest “Ideas are off” / “No saved idea yet” states.
- All widgets support light and dark palettes, follow Littly’s theme setting with System as the default, and use the same warm brand colors.
- The widget picker includes light/dark preview images for all three widgets, with `previewLayout` used on Android 12+ and `previewImage` as the launcher fallback.
- Widgets refresh after habit changes, completion toggles, idea generation, and theme changes. Android may still delay widget updates due to system host limits.

**Widget previews:** the widget picker uses light/dark preview layouts. The three widget providers are `LittlyWidget`, `StreakWidget`, and `IdeaWidget`.

## Streak rule

- Only goal days participate in a streak.
- A completed goal day extends the streak.
- A non-goal day neither extends nor breaks it.
- A still-pending **today** does not break the streak.
- Any missed past goal day breaks it.
- An empty goal-day set means every day is a goal day.

The current streak can therefore be non-zero before today’s check-in. This rule is shared by the habit cards, Today hero, Stats, and widgets. The all-time best streak also stores its start and end dates.

## Architecture

- **Kotlin** with **Jetpack Compose** and **Material 3**
- **Hilt** dependency injection
- **Room** for habits, completion logs, and idea history
- **DataStore** for settings
- **Kotlin Coroutines + Flow** for observable state
- **Jetpack Glance** for the three widgets
- **AlarmManager + BroadcastReceivers** for local reminders, with reboot rescheduling
- **MediaPipe GenAI** for the optional on-device Qwen3-0.6B model
- MVVM presentation layer with repositories and domain use cases

### Project structure

```
app/src/main/java/com/diego/littly/
├── core/
│   ├── data/            # Repository implementations, mappers, settings
│   ├── database/        # Room entities, DAOs, migrations
│   ├── designsystem/    # Tokens, Nunito typography, shared components, previews
│   ├── domain/          # Models, repositories, streak/stats calculations, use cases
│   ├── download/        # Optional model download
│   ├── llm/             # IdeaGenerator abstraction, MediaPipe, curated fallback
│   ├── navigation/      # Deep-link state
│   └── notifications/   # Alarms, receivers, notification helpers
├── feature/
│   ├── habits/          # List, detail, add/edit
│   ├── onboarding/      # Four-page first-run flow
│   ├── settings/        # Profile and settings
│   ├── stats/           # Monthly review
│   └── today/           # Daily dashboard
├── navigation/          # Root navigation and four-tab shell
└── widget/              # Overview, streak, and idea Glance widgets
```

### Local data and migration

The Room database is version 2. The 1→2 migration adds `description` and `iconKey` without dropping existing habits or completion logs. Destructive migration is not used in any build type. The migration is covered by a Robolectric test.

## Ideas and the optional model

- Idea generation is behind the `IdeaGenerator` interface.
- If the model is present, Littly can generate ideas on-device.
- If it is absent, the curated fallback supplies offline ideas. Those are labeled **Built-in**, never as AI-generated.
- Turning ideas off does not affect tracking, statistics, reminders, or widgets.
- Generated ideas are stored locally, and recent idea text is passed to generation to reduce repetition.
- No model inference runs when a notification fires. Notifications use a stored queued idea or a short default reminder message.
- Model download is isolated from core tracking. The app never requires the network to create, edit, complete, archive, or review habits.

## Reminders

- Reminders respect each habit’s enabled state, time, and reminder days (falling back to goal days).
- Reminders are rescheduled after boot, package replacement, and app start.
- Exact alarms are used when permitted; otherwise Android may delay reminders by a few minutes.
- Battery optimization and OEM background restrictions can affect delivery. Settings explains how to improve reliability.
- Notification permission is requested from onboarding or the reminder form with context.

## Build and run

Requirements:
- Android Studio Ladybug or newer
- JDK 21
- Android SDK 36
- Minimum Android 7.0 (API 24)

```bash
# Debug APK
./gradlew assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug

# Install on a connected device
./gradlew installDebug
```

If your shell’s `JAVA_HOME` points to an older JDK, override it, for example:

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew testDebugUnitTest
```

The optional model is downloaded from within the app. It is not required to build, test, or use the tracker.

## Testing

The unit suite covers:
- Streak calculations, including pending-today behavior and best-run date ranges
- Today, weekly, monthly, trend, and 30-day statistics
- Room persistence and the v1→v2 migration
- Habit editing, description/icon persistence, reminder days, and validation
- Reminder time/planner calculations
- Idea parsing, fallback behavior, anti-repeat logic, and source labels
- Widget data selection, responsive item limits, and widget theme resolution
- DataStore name/theme settings

Latest verification:
- `testDebugUnitTest`: **115 tests passed**
- `assembleDebug`: **successful**
- `lintDebug`: **successful, 0 errors**

No physical device or emulator was connected during the final verification, so runtime widget rendering and notification delivery were not device-tested in this environment. Widget metadata, preview assets, and preview layouts are covered by unit tests.

## Not implemented

- Cloud sync, accounts, social features, or cross-device backup
- Habit timers or duration tracking
- Idea metadata such as duration, tools, or tags
- Dynamic Material You color, intentionally omitted to protect the Littly palette
- User profile photos or authentication
- Server-side analytics

## Design references

The visual references used for layout and hierarchy are in `docs/`. Nunito is bundled under the SIL Open Font License; see `docs/Nunito-OFL.txt`.
