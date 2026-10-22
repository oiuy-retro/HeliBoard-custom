# Building HeliBoard Custom

This page explains how to build the APK, how it's signed, and what to do when the build fails.

## What you need

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 | Builds the app. The project targets Java 17. |
| JDK | 21 | Only for running the unit tests (Robolectric runs Android SDK 36 tests only on Java 21). |
| Android SDK | platform `android-37`, build-tools 36 | `compileSdk` and `targetSdk` are 37. |
| Android NDK | `28.0.13004108` | The keyboard has native code (dictionary and gesture typing). |
| Gradle | 9.7.1 | Comes with the wrapper (`./gradlew`). No separate install needed. |
| Android Gradle Plugin | 9.1.1 | Set in `build.gradle.kts`. |

If you accept the SDK licenses (`sdkmanager --licenses`), Gradle downloads missing SDK platforms and
the NDK by itself on the first build.

A first build downloads about 1–2 GB and takes a few minutes. Later builds take about 1 minute.

## Build the release APK

```bash
git clone https://github.com/oiuy-retro/HeliBoard-custom.git
cd HeliBoard-custom
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # path to your Android SDK
cp keystore.properties.example keystore.properties     # then edit it, see Signing
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleRelease
```

The APK is written to `app/build/outputs/apk/release/HeliBoard_4.3-beta1-release.apk`. Its package
name is `helium314.keyboard.custom`.

Check the signature and package before publishing:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/HeliBoard_4.3-beta1-release.apk
aapt dump badging app/build/outputs/apk/release/HeliBoard_4.3-beta1-release.apk | head -3
```

The certificate SHA-256 digest must be the one listed in the [README](../README.md#5-own-package-name-app-name-and-signing-key).

## Build a debug APK

`./gradlew assembleDebug` writes `app/build/outputs/apk/debug/HeliBoard_4.3-beta1-debug.apk`.
It uses the package `helium314.keyboard.custom.debug` and is signed with your computer's debug key
(`~/.android/debug.keystore`). As in upstream, it's minified and leaves out the Romanian dictionary
(`main_ro.dict`) to keep the APK small. Use it for testing only.

## Run the unit tests

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew :app:testDebugUnitTest
```

Expected result: 194 tests, **8 failures**. The same 8 fail on unmodified upstream:

- 5 tests that document known bugs: `CharacterIteratorTest.lastGraphemeFails`,
  `CharacterIteratorTest.firstGraphemeFails`, `InputLogicTest.insertLetterIntoWordHangulFails`,
  `StringUtilsTest.isEmojiFails`, `StringUtilsTest.detectEmojiAtStartFails`
- 3 link checks in `XLinkTest` (`layoutsLinks`, `readmeLinks`, `knownDictionaries`). They need
  internet access and check links that can break.

Upstream's `runTests` build type skips the known failures: `./gradlew :app:testRunTestsUnitTest`.

The test for this build's symbol layouts is `ParserTest` → `symbols layouts match OpenBoard` in
`app/src/test/java/helium314/keyboard/KeyboardParserTest.kt`.

## Signing

`app/build.gradle.kts` reads the release signing details from **`keystore.properties`** in the
repository root:

```properties
storeFile=/absolute/path/to/heliboard-custom-release.jks
storePassword=...
keyAlias=heliboard-custom
keyPassword=...
```

- `keystore.properties`, `*.jks` and `*.keystore` are in `.gitignore`. Never commit them. The
  committed template is [`keystore.properties.example`](../keystore.properties.example).
- If the file is missing or incomplete, release builds are signed with the debug key instead. Such an
  APK can't update an install signed with the release key.
- The release key is a PKCS12 keystore (RSA 4096, valid 10,000 days, alias `heliboard-custom`).
  PKCS12 keystores use one password for the store and the key, so `keyPassword` is the same as
  `storePassword`.
- Android only installs an update if it's signed with the same key. **Losing the keystore means
  existing installs can't be updated**: users would have to uninstall (after a backup) and reinstall.
  Keep a backup of the keystore and its passwords somewhere safe, outside the repository.
- To create a new key (only for a new, separate app):

  ```bash
  keytool -genkeypair -keystore heliboard-custom-release.jks -storetype PKCS12 \
    -alias heliboard-custom -keyalg RSA -keysize 4096 -validity 10000 \
    -dname "CN=oiuy-retro HeliBoard-custom"
  ```

## Publishing a release

Releases are GitHub prereleases with the signed release APK and a `SHA256SUMS` file. Tags are
named `custom-v1`, `custom-v2` and so on:

```bash
cp app/build/outputs/apk/release/HeliBoard_4.3-beta1-release.apk HeliBoard-custom-<tag>.apk
sha256sum HeliBoard-custom-<tag>.apk > SHA256SUMS
gh release create <tag> HeliBoard-custom-<tag>.apk SHA256SUMS \
  --repo oiuy-retro/HeliBoard-custom --target openboard-layout --prerelease
```

Add an entry to [CHANGELOG.md](../CHANGELOG.md) for each release.

## Troubleshooting

- **"SDK location not found"**: create `local.properties` with `sdk.dir=...`, or set `ANDROID_HOME`.
- **"Android SDK 36 requires Java 21"** when running tests: run the tests with JDK 21. Building still
  works with JDK 17.
- **License not accepted** for a platform or the NDK: run `sdkmanager --licenses` and build again.
- **Out of memory** during the build: add `-Dorg.gradle.jvmargs=-Xmx3g` to the Gradle command, or set
  it in `gradle.properties`.
- **"App not installed"** when updating: the new APK is signed with a different key (see Signing),
  for example because `keystore.properties` was missing and the debug key was used.
- **Install fails with a "conflicting provider"** error: another app uses the same provider
  authority. This build uses `helium314.keyboard.custom.*` authorities, so this only happens if two
  copies of this build with different package names are installed.
