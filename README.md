# DoseMate

Local medicine reminder and pill-tracking app for Android, built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room**, and **AlarmManager**.

## Features

- **Schedule** — Week date strip, Morning / Afternoon / Evening timeline, Take & Snooze actions
- **Cabinet** — Inventory grid with stock % rings, low-stock coral alerts, add-medication sheet
- **Settings** — Notification prefs, appearance, data & privacy (including app reset)
- **Reminders** — Exact alarms via `AlarmManager` + `DoseAlarmReceiver`, restored after reboot by `BootReceiver`

## Stack

| Layer | Tech |
|-------|------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (Schedule / Cabinet / Settings) |
| Architecture | MVVM + `StateFlow` |
| Persistence | Room (`Medication`, `Inventory`, `DoseSchedule`, `DoseLog`) |
| Prefs | DataStore |
| Notifications | AlarmManager + BroadcastReceivers |

## Project structure

```
app/src/main/java/com/dosemate/app/
├── data/
│   ├── alarm/          # AlarmScheduler, receivers, NotificationHelper
│   ├── local/          # Room DB, entities, DAOs
│   └── repository/     # MedicationRepository, SettingsRepository
├── ui/
│   ├── schedule/       # ScheduleScreen
│   ├── cabinet/        # CabinetScreen + add sheet
│   ├── settings/       # SettingsScreen
│   ├── navigation/     # Bottom nav + NavHost
│   ├── components/     # Shared composables
│   └── theme/          # Teal / coral DoseMate theme
├── viewmodel/          # Schedule, Cabinet, Settings ViewModels
├── DoseMateApp.kt
└── MainActivity.kt
```

## Open in Android Studio

1. Open this folder in Android Studio (Ladybug / Koala or newer recommended).
2. Sync Gradle (JDK 17 or 21).
3. Run the `app` configuration on an emulator or device (API 26+).

## Build from CLI

```bash
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Permissions

- `POST_NOTIFICATIONS` (Android 13+)
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`
- `RECEIVE_BOOT_COMPLETED`
- `VIBRATE`

## Sample data

On first launch Room seeds demo medications (Metformin, Lisinopril, Advil, etc.) with schedules and inventory so the UI matches the design screenshots immediately.
