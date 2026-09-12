# Sperren (Locker)

A tiny, UI-less Android app whose only job is to lock the screen the instant
it's launched.

It exists to be set as the target app for Pixel's **Quick Tap** gesture
(double-tap the back of the phone → "Open app"), so that a quick tap on the
back of the phone locks the screen — without triggering `DevicePolicyManager`
"administrator locked" behavior, so fingerprint/face unlock keep working
normally afterwards.

## Setup

1. **Install the app.** It has no launcher UI of its own — starting it just
   locks the screen immediately.
2. **Enable the accessibility service** (only needed once): Settings →
   Accessibility → *Downloaded apps* (wording varies by Android
   version/OEM, e.g. "Installed apps") → *Sperren* → turn it on. If you skip
   this, the app does it for you: launching it once opens this exact screen
   automatically (see `LockActivity`).
3. **Set it as the Quick Tap target** (Pixel only): Settings → System →
   Gestures → *Quick Tap to start actions* → turn on *Use Quick Tap* → choose
   *Open app* → tap the gear/settings icon next to it → select *Sperren*.
   ([Google's instructions](https://support.google.com/pixelphone/answer/7443425),
   available on Pixel 4a and newer.)
4. Double-tap the back of the phone → the screen locks.

Non-Pixel phones don't have Quick Tap, but `LockActivity` is a perfectly
normal launchable activity — it can be bound to any other gesture/shortcut
mechanism instead (e.g. a button-mapping app, Tasker, or a long-press
launcher shortcut).

## How it works

- `LockActivity` has no UI (`Theme.NoDisplay`). It's launched, immediately
  calls into the accessibility service to lock the screen, and finishes
  itself.
- `LockAccessibilityService` exists only so the app can call
  `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)`, an action that plain apps
  aren't allowed to perform. It does not read window content
  (`canRetrieveWindowContent="false"`), does not draw an overlay, and does not
  react to any accessibility event beyond the one type Android requires it to
  declare.
- The first time it's launched, if the accessibility service isn't enabled
  yet, the app shows a toast and opens the system accessibility settings so
  the user can turn it on once, by hand.

There is no network access, no analytics, no ads, and (beyond the platform
SDK) no third-party dependencies.

## Permissions

| Permission | Why |
| --- | --- |
| `BIND_ACCESSIBILITY_SERVICE` | Required by Android for any accessibility service; needed here purely to call `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)`. |

## Building

Standard Gradle/Android Studio project, no special setup required:

```sh
./gradlew assembleRelease
```

Requires JDK 17+ and Android SDK Platform 37 (installed automatically by
Gradle/AGP if missing and permitted).

## License

MIT, see [LICENSE](LICENSE). The app icon is original artwork, licensed the
same way as the rest of the repository.
