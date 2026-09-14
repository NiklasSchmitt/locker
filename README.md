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

A local release build is unsigned unless you provide signing credentials via
`keystore.properties` (copy `keystore.properties.example`, git-ignored) - see
[Releasing](#releasing).

## Releasing

Tagged pushes build a signed release APK and publish it as a GitHub release,
via [`.github/workflows/release.yml`](.github/workflows/release.yml):

1. Bump `versionCode`/`versionName` in `app/build.gradle.kts`.
2. Commit, then tag the commit with exactly the new `versionName` (no `v`
   prefix, e.g. `1.1`) and push the tag: `git tag 1.1 && git push origin 1.1`.
3. The workflow builds `Locker-<versionName>.apk`, signs it with the key
   from the `SIGNING_*` repository secrets, and attaches it (plus a
   `.sha256` checksum) to a new GitHub release named after the tag.

This "no v prefix" tag is also what's referenced as `commit:` in the
[F-Droid metadata](https://gitlab.com/fdroid/fdroiddata) for this app, and
the release asset URL (`.../releases/download/<tag>/Locker-<tag>.apk`) is
what its `Binaries:` field (`Binaries: .../releases/download/%v/Locker-%v.apk`)
points at, so that F-Droid can verify its own from-source build reproduces
this exact binary before distributing it.

### One-time setup: the signing key

The release key is permanent - every future release must be signed with the
same one, or app updates break for existing users and F-Droid's
`AllowedAPKSigningKeys` check fails. Generate it once and back it up
somewhere safe and durable (password manager + offline copy), e.g.:

```sh
keytool -genkeypair -v -keystore locker-release.jks -alias locker \
  -keyalg RSA -keysize 4096 -validity 10000
```

Then add these as repository secrets (Settings → Secrets and variables →
Actions → New repository secret):

| Secret | Value |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | `base64 -w0 locker-release.jks` |
| `SIGNING_STORE_PASSWORD` | the keystore password |
| `SIGNING_KEY_ALIAS` | `locker` (or whatever alias you chose) |
| `SIGNING_KEY_PASSWORD` | the key password |

For local signed builds, copy `keystore.properties.example` to
`keystore.properties` and fill in the same values (that file is git-ignored).

The signing certificate's SHA-256 fingerprint, needed for F-Droid's
`AllowedAPKSigningKeys`, can be read back out of the keystore with:

```sh
keytool -exportcert -alias locker -keystore locker-release.jks | \
  openssl dgst -sha256 -r | cut -d' ' -f1
```

## License

MIT, see [LICENSE](LICENSE). The app icon is original artwork, licensed the
same way as the rest of the repository.
