# HeliBoard — Custom Build

A personal build of the HeliBoard keyboard with an OpenBoard-style suggestion strip, OpenBoard's symbol layouts, a clipboard view without the bottom key row, a longer gesture trail and an inverted launcher icon. It installs next to official HeliBoard.

> **This is an unofficial custom build.** It is based on
> [HeliBoard](https://github.com/HeliBorg/HeliBoard) by Helium314 and the HeliBoard contributors
> (itself based on [OpenBoard](https://github.com/openboard-team/openboard) and the AOSP keyboard),
> and is distributed under the same [GNU General Public License v3.0](LICENSE). It is not
> affiliated with or supported by the upstream project — please don't report problems with this
> build to them.
>
> An overview of HeliBoard itself is in [docs/ABOUT_HELIBOARD.md](docs/ABOUT_HELIBOARD.md). Upstream's
> original README is at <https://github.com/HeliBorg/HeliBoard/blob/main/README.md>.

- Based on upstream `main` at commit `e97acdbe` (the `v4.3-beta1` tag, 2026-10-18).
- App version: **4.3-beta1**, versionCode **4300** (both unchanged from upstream).
- Package name **`helium314.keyboard.custom`**, app name **HeliBoard Custom**.
- Published builds are **release** builds, signed with this build's own key (see Signing).
- See [CHANGELOG.md](CHANGELOG.md) for the history of the custom releases.

## What's different from upstream

### 1. Top bar works like OpenBoard's suggestion strip

OpenBoard's strip has a 36 dp slot on the left, three suggestions, then a voice key and a clipboard
key (36 dp each). It has no expandable toolbar. This build copies that arrangement by changing
defaults, so all of HeliBoard's toolbar settings still work.

- **Toolbar mode** (Settings → Toolbar) now defaults to **"Suggestions only"**. There is no arrow
  key and no expandable toolbar.
- **Voice** and **clipboard** are pinned to the right of the suggestions by default.
  Tapping clipboard opens the clipboard history. Long-pressing it pastes, as in OpenBoard.
- The voice key only shows if a voice input keyboard is installed, and not in fields that block
  voice input, such as password fields (same as upstream).
- The left slot stays empty but keeps its space. It shows the incognito icon when incognito mode is on.
- Settings are still reachable by long-pressing the comma key, as in OpenBoard.
- Sizes follow OpenBoard: the edge keys are 54 dp wide in landscape (upstream: 48 dp), and the strip
  is 44 dp tall on screens 768 dp and wider (upstream: 40 dp). Other sizes were already the same.
- To get the expandable toolbar back, set Toolbar mode to "Toolbar keys and suggestions".

### 2. `?123` and `=\<` symbol layouts match OpenBoard

The `?123` page now has 4 rows instead of 5:

| Row | `?123` | `=\<` |
|---|---|---|
| 1 | `1 2 3 4 5 6 7 8 9 0` | ``~ ` \| • √ π ÷ × ¶ ∆`` |
| 2 | `@ # $ % & - + ( )` | `£ ¢ € ¥ ^ ° = { }` (other currencies depend on the language) |
| 3 | `=\<` `* " ' : ; ! ?` delete | `?123` `\ © ® ™ ℅ [ ]` delete |
| 4 | `ABC` `,` `_` space `/` `.` enter | `ABC` `,` `<` space `>` `.` enter |

- Long-press keys follow OpenBoard's defaults, for example `%` → `‰`, `-` → `_ – — ·`,
  `(` → `< { [`, `!` → `¡`, `?` → `¿`, `<` → `‹ ≤ «`.
- Upstream's **numpad key** is no longer in the symbols bottom row, because OpenBoard has none. The
  numpad is still available as a toolbar key (Settings → Toolbar → "Select pinned toolbar keys").
- The **D-pad key** (Settings → Preferences → "D-pad key") is now **off by default**. If you turn it
  on, it appears next to `/` on the `?123` page.
- **Letter long-press symbols changed.** HeliBoard takes the symbol you get by long-pressing a letter
  from the symbols layout. The top letter row now offers ``~ ` | • √ π ÷ × ¶ ∆`` (upstream:
  `% \ | = [ ] < > { }`), and the middle row offers `@ # $ % & - + ( )`.
- If you turn off "Show number row in symbols view", the `~ ` | …` row replaces the numbers on the `?123` page.
  This is what OpenBoard shows below its number row when the number row is enabled.
- The Arabic symbols layout uses the same rows with its Arabic characters kept.
- **Tablets: partly done.** Only the `<` and `>` keys were added to the tablet bottom row. The rest of
  the tablet layout is upstream's, because OpenBoard uses separate tablet layouts that weren't ported.

### 3. Clipboard view has no bottom key row

- The clipboard history no longer has the bottom row (ABC, space bar, delete). The list of clips
  uses the freed space.
- To go back to the letters, tap the **ABC** key on the left of the strip above the clips. The label
  is the same as on the normal ABC key, so it changes with the language (for example `АБВ`).
- The **clear history** key is now at the right edge of that strip, as in OpenBoard. The other
  clipboard toolbar keys (arrows, undo, cut, copy, paste, select word) are still there to its left.
- The "close" (X) key is no longer shown by default, because the ABC key replaces it. You can turn it
  back on in Settings → Toolbar → "Select clipboard toolbar keys".
- **Edge case:** if Toolbar mode is "Hidden" and "Hide clipboard and emoji toolbars too" is on
  (the default for that option), there is no strip to put the ABC key in. The bottom row then stays,
  so you can still get back to the letters.
- The emoji view is unchanged and keeps its bottom row.

### 4. Inverted launcher icon

- The launcher icon colors are inverted (red–purple gradient with white symbols → green gradient
  with black symbols). This makes the custom build easy to tell apart from official HeliBoard.
- Inverted: the adaptive icon's foreground and background, all `mipmap-*` PNGs, and the store icon in
  `fastlane/`. The themed (monochrome) icon on Android 13+ keeps its shape, because Android colors it.
- The legacy PNG icons had a faint dark drop shadow. It's inverted too, so it's now a faint light edge.

### 5. Own package name, app name and signing key

- Package name `helium314.keyboard.custom` (upstream: `helium314.keyboard`), so it installs next to
  official HeliBoard instead of replacing it.
- App and keyboard name **HeliBoard Custom**. The spell checker is "HeliBoard Custom Spell Checker"
  in English. Other languages keep upstream's translated names.
- The internal file providers (for pasting clipboard images and sharing gesture data) got their own
  names (`helium314.keyboard.custom.clipprovider` and `helium314.keyboard.custom.provider`), so they
  don't clash with official HeliBoard's.
- Release builds are signed with this build's own release key. Its SHA-256 certificate fingerprint is:
  `8C:4D:B0:D6:55:51:73:3C:F9:09:C5:22:CE:3B:37:E3:1A:E1:11:76:31:58:07:0F:AA:32:51:78:60:27:9E:EF`
- The Java/Kotlin code still uses the `helium314.keyboard` package names, so upstream changes merge
  as before.

### 6. Longer gesture trail

- The **Gesture trail lifespan** slider (Settings → Gesture typing) now goes up to **20,000 ms (20 s)**
  instead of 2,000 ms. The minimum (200 ms) and the default (900 ms) are unchanged.
- The slider isn't linear: the old range up to 2 s takes about the first 30 % of it, in 10 ms steps.
  Above 2 s it moves in 50 ms steps.
- The word preview that floats above the trail still disappears after at most 475 ms, as before.
  Upstream ties it to a quarter of the trail lifespan, so without this limit it could stay for 5 s.
- While a trail is visible the keyboard redraws it about 50 times a second (same as upstream). A very
  long lifespan therefore keeps that redraw going longer and uses a bit more battery.

### Existing settings

The toolbar, pinned keys, clipboard toolbar keys and D-pad changes above are new **defaults**. If you
restore a backup from official HeliBoard (see Install), any of these settings you had changed there
come back as you set them. Settings you never changed get this build's defaults. The symbol layouts
and the clipboard bottom row change apply in any case.

## Install

Requirements: **Android 5.0 (API 21) or newer.**

1. Download the latest `.apk` from this repository's
   [Releases](https://github.com/oiuy-retro/HeliBoard-custom/releases) page. APK files are never
   committed to the repository. Each release also has a `SHA256SUMS` file to check the download
   (`sha256sum -c SHA256SUMS`).
2. The package name is `helium314.keyboard.custom`, so it installs **alongside** official HeliBoard
   (`helium314.keyboard`). Nothing has to be uninstalled.
3. **If you installed an earlier test build** (`openboard-layout-debug-1`, shown as "HeliBoard debug",
   package `helium314.keyboard.debug`), uninstall it. It's a different app and won't be updated.
   Back up its settings first if you want to keep them (step 6).
4. Open the APK on your phone. If asked, allow your browser or file manager to
   **install unknown apps** (Settings → Apps → Special app access → Install unknown apps).
5. Enable **HeliBoard Custom** in Android's keyboard settings and pick it as the current keyboard.
6. To copy your settings from official HeliBoard or the test build, use Settings → Advanced →
   **Backup and restore** there, then restore the file in HeliBoard Custom. Toolbar settings you had
   changed there come back (see "Existing settings" above).
7. Later releases install as normal updates over earlier ones, because they're signed with the same
   release key.

## Building from source

Short version (details and troubleshooting are in [docs/BUILDING.md](docs/BUILDING.md)):

- **JDK 17** to build. Running the unit tests needs **JDK 21** (Robolectric).
- **Android SDK** with platform **android-37** and **NDK 28.0.13004108** (the keyboard has native
  code). Gradle downloads missing SDK parts itself if the licenses are accepted.
- The Gradle wrapper (Gradle 9.7.1) is checked in, so you don't need your own Gradle install.

```bash
git clone https://github.com/oiuy-retro/HeliBoard-custom.git
cd HeliBoard-custom
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # path to your Android SDK
cp keystore.properties.example keystore.properties   # then edit it, see Signing
./gradlew assembleRelease
# -> app/build/outputs/apk/release/HeliBoard_4.3-beta1-release.apk
```

### Signing

`app/build.gradle.kts` reads the release signing details from a file called **`keystore.properties`**
in the repository root. That file is listed in `.gitignore` and must **never** be committed.
[`keystore.properties.example`](keystore.properties.example) is the template:

```properties
storeFile=/absolute/path/to/heliboard-custom-release.jks
storePassword=your-store-password
keyAlias=heliboard-custom
keyPassword=your-key-password
```

If `keystore.properties` is missing, release builds are signed with the Android **debug** key instead.
They still install, but they can't update an APK signed with the release key. To update an existing
install, always sign with the **same keystore** as before, and keep a backup of it. Keystores
(`*.jks`, `*.keystore`) are also in `.gitignore`.

Debug builds (`./gradlew assembleDebug`) are always signed with the debug key and use the package
`helium314.keyboard.custom.debug`, so they install as a separate app.

## Known limitations

- Testing: each release is built with `./gradlew assembleRelease` and checked with the unit tests.
  One unit test checks the new symbol layouts. The top bar and symbol changes were tried by hand on one
  phone (in the earlier debug test build). The clipboard change, the icon and the new package name
  were built and unit-tested but not yet tried on a device. There is no automated on-device testing.
- Unit tests: 194 run, 8 fail. The same 8 fail on unmodified upstream at `e97acdbe`: 5 tests marked
  as known failures (names ending in `Fails`) and 3 link checks (`XLinkTest`) that need internet access.
- Only the **release** variant is published. Upstream's other build types (`nouserlib`, `runTests`,
  `debugNoMinify`) still build but haven't been tried. `nouserlib` isn't signed.
- Tablet symbol layouts only partly follow OpenBoard (see section 2).
- The numpad key is gone from the symbols bottom row. Pin the numpad toolbar key if you use it.
- Letter long-press symbols differ from upstream (see section 2).
- Backups restored from official HeliBoard bring back toolbar settings you had changed (see "Existing settings").
- The earlier test build (`helium314.keyboard.debug`) can't be updated to this one. Uninstall it (see Install).
- Settings and learned words don't move over automatically from official HeliBoard. Use Backup and restore.

## Staying in sync with upstream

This fork keeps upstream's history, so new upstream versions can be merged in. Upstream's default
branch is `main`, and releases are tagged like `v4.3-beta1`. The short version:

```bash
git remote add upstream https://github.com/HeliBorg/HeliBoard.git   # once
git fetch upstream --tags
git checkout openboard-layout
git merge upstream/main          # or a release tag, e.g. v4.3
# resolve conflicts, build, run the tests, push, publish a release
```

The full walkthrough is in [docs/UPDATING_FROM_UPSTREAM.md](docs/UPDATING_FROM_UPSTREAM.md). It
lists the files that tend to conflict and how to publish a release.

## More documentation

- [CHANGELOG.md](CHANGELOG.md): what changed in each custom release
- [docs/BUILDING.md](docs/BUILDING.md): build, signing and troubleshooting
- [docs/CUSTOM_CHANGES.md](docs/CUSTOM_CHANGES.md): technical details of the custom changes
- [docs/UPDATING_FROM_UPSTREAM.md](docs/UPDATING_FROM_UPSTREAM.md): merging new upstream versions
- [docs/ABOUT_HELIBOARD.md](docs/ABOUT_HELIBOARD.md): what HeliBoard is, its features, and links to
  upstream's own documentation

## License and credits

HeliBoard is © Helium314 and the HeliBoard contributors, licensed under the
[GNU GPL v3.0](LICENSE). It is based on OpenBoard and the AOSP keyboard
([Apache 2.0](LICENSE-Apache-2.0)). This custom build is a modified version and is under the same
licenses. Source files keep their original copyright headers.

The launcher icon is by [Fabian OvrWrt](https://github.com/FabianOvrWrt) with contributions from
[The Eclectic Dyslexic](https://github.com/the-eclectic-dyslexic), licensed under
[CC BY-SA 4.0](LICENSE-CC-BY-SA-4.0). This build inverts its colors. The modified icon is under the same license.

The layouts and strip arrangement copied in this build come from
[OpenBoard](https://github.com/openboard-team/openboard) (GPL-3.0). All credit for the app itself
goes to the upstream authors: <https://github.com/HeliBorg/HeliBoard>.
