# Updating from upstream

This page explains how to bring new HeliBoard versions into this custom build.

## Branches and remotes

- `origin` is <https://github.com/oiuy-retro/HeliBoard-custom> (this fork).
- `upstream` is <https://github.com/HeliBorg/HeliBoard>. Its default branch is `main`, and releases
  are tagged like `v4.3-beta1`.
- The custom changes are on `openboard-layout`. Once PR #1 is merged, they're on `main` too.
  Use whichever branch holds the custom changes.

## Steps

1. Add the upstream remote (only once) and fetch:

   ```bash
   git remote add upstream https://github.com/HeliBorg/HeliBoard.git
   git fetch upstream --tags
   ```

2. See what's new upstream:

   ```bash
   git log --oneline openboard-layout..upstream/main
   ```

3. Merge, either the latest `main` or a release tag:

   ```bash
   git checkout openboard-layout
   git merge upstream/main        # or: git merge v4.3
   ```

4. Resolve conflicts (see the list below). For layout files, keep OpenBoard's arrangement and
   carry over new upstream keys or options only if they don't change the arrangement.

5. Build and test (see [BUILDING.md](BUILDING.md)):

   ```bash
   ./gradlew assembleDebug
   ./gradlew :app:testDebugUnitTest   # with JDK 21
   ```

   Check that `symbols layouts match OpenBoard` passes, and that no tests fail beyond upstream's own
   known failures. To see upstream's own failures, run the tests on `upstream/main` too.

6. Update the README header (upstream commit, tag, version name and versionCode) and add an entry to
   [CHANGELOG.md](../CHANGELOG.md).

7. Build the release APK with the release key (`keystore.properties` must be present, see
   [BUILDING.md](BUILDING.md#signing)), then push and publish a prerelease (commands in
   [BUILDING.md](BUILDING.md#publishing-a-release)).

To keep the fork's `main` equal to upstream before merging, you can also run
`gh repo sync oiuy-retro/HeliBoard-custom --branch main` (only while `main` has no custom commits).

## Files that tend to conflict

| File | Why |
|---|---|
| `app/src/main/assets/layouts/symbols/symbols.txt`, `symbols_arabic.txt` | Rows rearranged to OpenBoard's. |
| `app/src/main/assets/layouts/more_symbols/symbols_shifted.txt` | Last row moved into the functional keys. |
| `app/src/main/assets/layouts/functional/functional_keys*.json` | Bottom row changed. Upstream edits these often. |
| `app/src/main/java/helium314/keyboard/latin/settings/Defaults.kt` | Toolbar mode and D-pad defaults. |
| `app/src/main/java/helium314/keyboard/latin/utils/ToolbarUtils.kt` | Default pinned and clipboard toolbar keys. If upstream adds a toolbar key, keep the new defaults. |
| `app/src/main/java/helium314/keyboard/latin/suggestions/SuggestionStripView.kt` | `updateKeys()` visibility change. |
| `app/src/main/java/helium314/keyboard/keyboard/clipboard/ClipboardHistoryView.kt`, `ClipboardLayoutParams.kt` | Bottom row hidden, ABC key. |
| `app/src/main/java/helium314/keyboard/keyboard/KeyboardSwitcher.java`, `app/src/main/res/layout/strip_container.xml` | Clipboard strip container. |
| `app/src/main/res/values-land/config.xml`, `values-sw768dp*/config.xml` | Strip dimensions. |
| `app/build.gradle.kts` | `applicationId` and release signing. Upstream often bumps versions and dependencies here. |
| `app/src/main/res/values/clip_provider.xml`, `gesture_data.xml`, `donottranslate.xml`, `strings.xml` | Provider authorities and app name. If upstream adds a new provider, give it a `helium314.keyboard.custom.` authority too. |
| `app/src/test/java/helium314/keyboard/Shadows.kt` | Test package names. |
| `app/src/main/java/helium314/keyboard/settings/screens/GestureTypingScreen.kt`, `MainKeyboardView.java` | Gesture trail lifespan slider and floating preview linger cap. |
| Launcher icon files (see [CUSTOM_CHANGES.md](CUSTOM_CHANGES.md#4-launcher-icon)) | If upstream changes its icon, keep upstream's new icon and invert it again. |
| `README.md` | This repository's README is the custom build's own. Upstream's README isn't copied. If upstream changes its README, keep ours, and update [ABOUT_HELIBOARD.md](ABOUT_HELIBOARD.md) if HeliBoard's features or links changed. |

## Inverting the icon again

If upstream changes the icon, invert the new files the same way. For PNGs (keeps alpha):

```bash
magick icon.png -channel RGB -negate +channel icon.png
```

For the vector drawables, replace each `#RRGGBB` / `#AARRGGBB` color with 255 minus each RGB value,
keeping the alpha.
