# Diet Reminder

An Android app that reminds you to eat on schedule and proves you did: at each meal time a full-screen alarm rings, and you dismiss it by taking a photo of your meal. It also reminds you to take your medicines.

## Features

- **Meal alarms** for breakfast, lunch, dinner and snacks, set to your own times. They ring as a full-screen alarm, including on the lock screen.
- **Photo logging**: you dismiss the alarm by snapping your meal with the camera.
- **Medicine reminders**: add medicines with a dosage and a daily reminder time.
- **Dashboard** with today's meals and medicines.
- **History** of the last 30 days of meals and photos.
- **Setup wizard** for your profile (age, weight, height, and a goal of weight loss, weight gain or maintenance) and your meal times.
- Alarms come back automatically after a reboot.
- Everything stays on the device: no account, no network, and photos are kept in the app's private storage.

## Tech stack

- Kotlin and Jetpack Compose (Material 3)
- MVVM with Hilt for dependency injection
- Room database, plus DataStore for preferences
- CameraX for photos and Coil for loading images
- `AlarmManager.setAlarmClock()` for exact alarms that Doze and battery savers don't delay

## Requirements

- Android 8.0 (API 26) or newer; the app targets API 35
- JDK 17
- Android Studio (Ladybug or newer) or the Android SDK command-line tools

## Build and run

```bash
git clone <your-repo-url>
cd diet_tracking
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To install it on a connected device:

```bash
./gradlew installDebug
```

Or open the project in Android Studio and press **Run**.

## Permissions

| Permission | Why |
|---|---|
| `CAMERA` | Taking meal photos |
| `POST_NOTIFICATIONS` | Alarm and reminder notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Alarms that go off at the exact minute |
| `USE_FULL_SCREEN_INTENT` | Showing the alarm screen over the lock screen |
| `RECEIVE_BOOT_COMPLETED` | Setting the alarms again after a reboot |
| `VIBRATE`, `WAKE_LOCK` | Vibrating and waking the device for alarms |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keeping reminders reliable on phones with aggressive battery savers |

> **Tip:** on Samsung, Xiaomi, OnePlus and similar phones, set the app's battery usage to *Unrestricted*, or alarms may be delayed or skipped.

## Project structure

```
app/src/main/java/com/dietreminder/
├── alarm/       # AlarmScheduler, meal/medicine alarm receivers, BootReceiver
├── data/        # Room database, entities, DAOs, repositories
├── di/          # Hilt modules
├── ui/
│   ├── alarm/       # Full-screen alarm and camera
│   ├── dashboard/   # Today's overview
│   ├── history/     # Past 30 days
│   ├── medicine/    # Medicine management
│   ├── settings/    # Meal times and profile
│   ├── setup/       # First-run wizard
│   └── navigation/  # NavGraph
└── util/        # Time and image helpers
```
