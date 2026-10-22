# Custom changes: technical details

This page lists what the custom build changes in the code, file by file. For the user-facing
description, see the [README](../README.md).

All changes are on the branch `openboard-layout`. The reference is OpenBoard at
<https://github.com/openboard-team/openboard> (default branch).

## 1. Suggestion strip (top bar)

OpenBoard sources matched: `app/src/main/res/layout/suggestions_strip.xml`,
`.../latin/suggestions/SuggestionStripView.java`, `res/values*/config.xml`.

| File | Change |
|---|---|
| `app/src/main/java/helium314/keyboard/latin/settings/Defaults.kt` | `PREF_TOOLBAR_MODE` default `EXPANDABLE` → `SUGGESTION_STRIP`. `PREF_SHOW_DPAD_KEY` default `true` → `false`. |
| `app/src/main/java/helium314/keyboard/latin/utils/ToolbarUtils.kt` | `defaultPinnedToolbarPref` enables `VOICE` and `CLIPBOARD` (in that order). `defaultClipboardToolbarPref`: `CLEAR_CLIPBOARD` moved to the end, `CLOSE_HISTORY` disabled. |
| `app/src/main/java/helium314/keyboard/latin/suggestions/SuggestionStripView.kt` | `updateKeys()`: in `SUGGESTION_STRIP` mode the expand key is `INVISIBLE` instead of `GONE`, so the left slot keeps its width like OpenBoard's `suggestions_strip_other_key`. It still shows the incognito icon. |
| `app/src/main/res/values-land/config.xml` | `config_suggestions_strip_edge_key_width` 48 dp → 54 dp. |
| `app/src/main/res/values-sw768dp/config.xml`, `values-sw768dp-land/config.xml` | `config_suggestions_strip_height` 40 dp → 44 dp. |

## 2. Symbol layouts

OpenBoard sources matched: `res/xml/rows_symbols.xml`, `rowkeys_symbols1.xml`, `rowkeys_symbols2.xml`,
`rowkeys_symbols3.xml`, `row_symbols4.xml`, `rows_symbols_shift.xml`, `rowkeys_symbols_shift1.xml`,
`rowkeys_symbols_shift2.xml`, `rowkeys_symbols_shift3.xml`, `row_symbols_shift4.xml`,
`keys_parentheses.xml`, `key_styles_less_greater.xml`, and the defaults in
`KeyboardTextsTable.java` for long-press keys.

| File | Change |
|---|---|
| `app/src/main/assets/layouts/symbols/symbols.txt` | 3 rows: ``~ ` \| • √ π ÷ × ¶ ∆`` (replaced by the number row by default), `@ # $ % & - + ( )`, `* " ' : ; ! ?`. |
| `app/src/main/assets/layouts/symbols/symbols_arabic.txt` | Same structure, Arabic characters kept. |
| `app/src/main/assets/layouts/more_symbols/symbols_shifted.txt` | The 2-key `< >` row removed (it used to replace comma and period). `=` popup without `⁼`, `℅` without `%`. |
| `app/src/main/assets/layouts/functional/functional_keys.json` | Bottom row: `_` / `/` next to space on `?123`, `<` / `>` (with `‹ ≤ «` / `› ≥ »` popups) on `=\<`. Numpad key removed. D-pad key (if enabled) after `/`. |
| `app/src/main/assets/layouts/functional/functional_keys_khipro.json` | Same as above. |
| `app/src/main/assets/layouts/functional/functional_keys_tablet.json` | Only `<` / `>` added on `=\<`. |
| `app/src/test/java/helium314/keyboard/KeyboardParserTest.kt` | New test `symbols layouts match OpenBoard`. |

How HeliBoard builds the `?123` page: the first row of `symbols.txt` is replaced by the number row
when "Show number row in symbols view" is on (default). The other rows come from `symbols.txt`, and
the functional keys (shift, delete, bottom row) from `functional_keys.json`. The first row also feeds
the symbol long-press keys on the top letter row (`KeyboardParser.addSymbolPopupKeys`).

## 3. Clipboard view

OpenBoard source compared: `res/layout/clipboard_history_view.xml` (ABC text key on the left,
clear key on the right, no space or delete keys).

| File | Change |
|---|---|
| `app/src/main/res/layout/strip_container.xml` | The clipboard strip's `HorizontalScrollView` is wrapped in a horizontal `LinearLayout` (`clipboard_strip_container`) with a fixed `TextView` (`clipboard_strip_alpha_key`) on the left. `fillViewport` is on, so the toolbar keys sit at the right edge. |
| `app/src/main/java/helium314/keyboard/keyboard/KeyboardSwitcher.java` | Shows and hides `clipboard_strip_container` instead of the scroll view. New getter `getClipboardStripAlphaKey()`. |
| `app/src/main/java/helium314/keyboard/keyboard/clipboard/ClipboardLayoutParams.kt` | New `withBottomRow` parameter and `showsClipboardBottomRow()` (true only if the secondary strip is hidden). Without the bottom row, the list height is the keyboard height minus the bottom padding. |
| `app/src/main/java/helium314/keyboard/keyboard/clipboard/ClipboardHistoryView.kt` | `setupBottomRowKeyboard()` still builds the bottom row, but hides it when the strip is visible. The ABC key takes its label from the bottom row's `ALPHA` key (so it's localized), uses the same text size, typeface and color, and sends `KeyCode.ALPHA`. |

The emoji view is not changed. It shares only the bottom-row builder
(`KeyboardLayoutSet.Builder.buildEmojiClipBottomRow`), which is unchanged.

## 4. Launcher icon

Colors inverted (RGB → 255 − RGB, alpha kept):

- `app/src/main/res/drawable/ic_launcher_foreground.xml`: `#ffffff` → `#000000`.
- `app/src/main/res/drawable-v24/ic_launcher_background.xml`: gradient `#FFCB2852` → `#FF34D7AD`,
  `#FF7473E4` → `#FF8B8C1B`.
- `app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.png` and `ic_launcher_round.png`.
- `fastlane/metadata/android/en-US/images/icon.png`.

The adaptive icon's monochrome layer uses the same foreground drawable. Android tints it with the
theme color, so only its shape matters and that's unchanged. Transparent stroke colors
(`#00000000` → `#00FFFFFF`) stay invisible.

## 5. Gesture trail lifespan

| File | Change |
|---|---|
| `app/src/main/java/helium314/keyboard/settings/screens/GestureTypingScreen.kt` | The `PREF_GESTURE_TRAIL_FADEOUT_DURATION` setting uses a custom slider (`GestureTrailLifespanPreference`) instead of `SliderPreference`. The slider position `p` (0–1) maps to a fade-out of `100 + 19800 × p²` ms, rounded to 10 ms below 1900 ms and to 50 ms above. The shown lifespan adds the 100 ms fade-out start delay, so 200 ms – 20 s. The stored value and the default (800, shown as 900 ms) are unchanged. |
| `app/src/main/java/helium314/keyboard/keyboard/MainKeyboardView.java` | `mGestureFloatingPreviewTextLingerTimeout = min(duration / 4, 475)`. 475 ms is the old maximum (1900 / 4). |

Checked for long durations, no change needed:

- `GestureTrailDrawingPoints.getAlpha()` computes `fullAlpha × elapsed / duration` in `int`. With at
  most 255 × 20,000 that's far below the `int` limit. `getWidth()` uses `float`.
- Event times are `int` milliseconds relative to the stroke start, so 20 s is no problem.
- Old trail points are dropped once they are older than the linger time, and the arrays are compacted.
  With a long lifespan more points are kept (`ResizableIntArray` grows as needed). That's a few
  thousand ints for several quick words, so memory use stays small.
- `GestureTrailsDrawingPreview` keeps redrawing every 20 ms (`config_gesture_trail_update_interval`)
  while any trail is visible, the same as upstream, just for longer.

## 6. Package name, app name and signing

| File | Change |
|---|---|
| `app/build.gradle.kts` | `applicationId` `helium314.keyboard` → `helium314.keyboard.custom` (debug builds add `.debug`). Reads `keystore.properties` and signs the `release` build type with it, or with the debug key if the file is missing. Minify and other settings unchanged. |
| `.gitignore` | Adds `keystore.properties`, `*.jks`, `*.keystore`. |
| `keystore.properties.example` | Template for `keystore.properties`. |
| `app/src/main/res/values/clip_provider.xml`, `gesture_data.xml` | Provider authorities `helium314.keyboard.custom.clipprovider` and `helium314.keyboard.custom.provider`, so they don't clash with official HeliBoard. |
| `app/src/debug/res/values/*.xml`, `app/src/debugNoMinify/res/values/*.xml` | Debug authorities `helium314.keyboard.custom.debug.*` and labels "HeliBoard Custom debug". |
| `app/src/main/res/values/donottranslate.xml`, `strings.xml` | App name "HeliBoard Custom". English spell checker and settings names "HeliBoard Custom Spell Checker" and "HeliBoard Custom Settings". |
| `app/src/test/java/helium314/keyboard/Shadows.kt` | The fake input method list in unit tests uses the new package names. |

The `namespace` (`helium314.keyboard.latin`) and all Java/Kotlin package names are unchanged.
Component names in the manifest and in `res/xml/method.xml` / `spellchecker.xml` are class names,
not the application id, so they work with the new id. The public broadcast action
`helium314.keyboard.latin.ACTION_SEND_INTENT` (for automation apps) is unchanged.
