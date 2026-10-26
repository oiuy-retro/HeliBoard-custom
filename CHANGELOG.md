# Changelog

All notable changes to this custom build of HeliBoard are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

All custom releases keep upstream's version name **4.3-beta1** (`versionCode` 4300 in `custom-v1`,
4301 in `custom-v2`, 4302 in `custom-v3`, 4303 in `custom-v4`, 4304 in `custom-v5`) and are built from upstream `main` at
`e97acdbe` (the `v4.3-beta1` tag). They're
published as GitHub prereleases and told apart by their tag. From `custom-v1` on, they're release
builds with the package `helium314.keyboard.custom`. The earlier test build was a debug build
(`helium314.keyboard.debug`). For changes in the app itself, see the
[upstream project](https://github.com/HeliBorg/HeliBoard).

## [custom-v5] - 2026-10-22

### Changed
- **Clipboard strip matches OpenBoard:** the strip above the clipboard history now holds only the
  ABC key on the left, to go back to the letters, and the clear history key on the right, which deletes
  the whole clipboard history. The arrow, undo, cut, copy, paste, select all, select word, settings and
  close keys are gone. This applies to existing installs too, because stored clipboard toolbar key
  settings are ignored.
- The "Select clipboard toolbar keys" setting in Settings → Toolbar is hidden, since the clipboard
  strip is fixed.
- `versionCode` bumped from `4303` to `4304` so `custom-v5` installs cleanly as an update over
  `custom-v4`.

## [custom-v4] - 2026-10-23

### Added
- **Gesture trail color brightness** slider in Settings → Gesture typing (`0 %` – `100 %`, default `80 %`
  matching `#80CBC4`'s HSV value `203 / 255 ≈ 79.6 %`).
- **Default tick mark on all settings sliders:** every slider in Settings (`SliderPreference`, the
  gesture trail lifespan slider, and `KeyboardScalePreference` dialogs) draws a vertical tick mark on the
  track at the position of its default value.
- **Per-step light vibration and stronger default-snap vibration:** dragging any settings slider gives a
  light haptic tick (`5 ms`, amplitude `65`) on every change by 1 unit (`1 %`, `1°`, `1 ms` or 1 step) and
  a noticeably stronger haptic pulse (`28 ms`, full amplitude `255`) when snapping onto the default value.

### Changed
- Gesture trail color sliders (`saturation`, `brightness` and `angle step`) use smooth slider tracks
  without `stepSize` tick dots, matching all other settings sliders in HeliBoard.
- `versionCode` bumped from `4302` to `4303` so `custom-v4` installs cleanly as an update over
  `custom-v3`.

## [custom-v3] - 2026-10-23

### Added
- **Gesture trail color saturation** slider in Settings → Gesture typing (`0 %` – `100 %`, `1 %` steps,
  default `37 %` matching OpenBoard's `#80CBC4` light teal).
- **Gesture trail color angle step** slider in Settings → Gesture typing (`0°` – `180°`, `1°` steps,
  default `138°` matching the golden angle ~`137.5°`; `0°` keeps the default teal hue on every swipe).
- **Default-value snap and haptic feedback on all settings sliders:** every slider in Settings
  (`SliderPreference`, the gesture trail lifespan slider, and the multi-slider `KeyboardScalePreference`
  dialogs) snaps to its default value within `±2 %` of the slider's span and vibrates with a short crisp
  haptic tick when landing on the default value.
- Unit tests in `GestureTrailDrawingPointsTest` for custom saturation/angle steps and slider default
  snapping.

### Changed
- `versionCode` bumped from `4301` to `4302` so `custom-v3` installs cleanly as an update over
  `custom-v2`.

## [custom-v2] - 2026-10-22

### Changed
- **Gesture trail changes color on every swipe.** Each new gesture stroke steps the hue by the golden
  angle (~137.5°) starting from OpenBoard's default light teal (`#80CBC4`), keeping the same light HSV
  saturation (36.9 %) and value (79.6 %) so every swipe is a distinct light pastel color.
- **Lingering gesture trails keep their own color.** `GestureTrailDrawingPoints` now stores the stroke
  color per point (`mPointColors`), so when multiple trails stay on screen at once (up to 20 s),
  earlier strokes keep the color they were swiped with while fading out.
- `versionCode` bumped from `4300` to `4301` so `custom-v2` installs cleanly as an update over
  `custom-v1`.
- `docs/UPSTREAM_README.md` (a copy of upstream's README) is replaced by
  [docs/ABOUT_HELIBOARD.md](docs/ABOUT_HELIBOARD.md), a short description of HeliBoard in this
  repository's style: what it is, its features (with notes where this build differs), links to
  upstream's README, wiki, FAQ and contributing guide, and the licenses. Links in the README and
  docs are updated.
- README: blockquote and "Building from source" wording aligned with the other custom repositories,
  and updated for the per-swipe gesture trail colors.

### Added
- Unit tests in `GestureTrailDrawingPointsTest` checking the per-swipe light color sequence and that
  lingering strokes retain their own color when new strokes are added and drawn.

## [custom-v1] - 2026-10-22

### Changed
- **New package name `helium314.keyboard.custom`** and app name **HeliBoard Custom**. It installs
  next to official HeliBoard. The earlier test build (`helium314.keyboard.debug`) is a different app.
  Uninstall it after moving its settings with Backup and restore.
- Release builds are signed with a dedicated release key (read from the gitignored
  `keystore.properties`, see `keystore.properties.example`). Without that file, they fall back to the
  debug key.
- File provider authorities renamed to `helium314.keyboard.custom.*` so they don't clash with
  official HeliBoard.
- Clipboard view: the bottom key row (ABC, space, delete) is removed and the clip list uses the
  freed height. An **ABC** key on the left of the clipboard strip goes back to the letters, and the
  clear history key is at the strip's right edge, as in OpenBoard. The bottom row still shows if
  Toolbar mode is "Hidden" with "Hide clipboard and emoji toolbars too" on, because then there's no strip.
- Default clipboard toolbar keys: clear history moved to the right end. The close (X) key is off by
  default because the ABC key replaces it.
- Gesture trail lifespan slider goes up to 20,000 ms (upstream: 2,000 ms). Same minimum (200 ms)
  and default (900 ms). The slider is non-linear (the old range is the first ~30 %), 10 ms steps up
  to 2 s and 50 ms above. The floating word preview's linger time is capped at 475 ms (the old
  maximum), so it doesn't stay for seconds with long trails.
- Launcher icon colors inverted (adaptive icon foreground and background, `mipmap-*` PNGs and the
  fastlane store icon).

### Added
- `keystore.properties.example` and `.gitignore` entries for signing files (`keystore.properties`,
  `*.jks`, `*.keystore`).
- Documentation for the custom build: README, this changelog, `docs/BUILDING.md`,
  `docs/CUSTOM_CHANGES.md` and `docs/UPDATING_FROM_UPSTREAM.md`. The upstream README was moved to
  `docs/UPSTREAM_README.md` (replaced later, see Unreleased).
- `SHA256SUMS` file attached to the release.

## [openboard-layout-debug-1] - 2026-10-22

Debug test build, package `helium314.keyboard.debug`.

### Changed
- Top bar: Toolbar mode defaults to "Suggestions only" (no expandable toolbar), and voice and
  clipboard are pinned to the right of the suggestions. The left slot keeps its space and only shows
  the incognito icon. Strip sizes match OpenBoard in landscape and on screens 768 dp and wider.
- `?123` and `=\<` layouts match OpenBoard's rows, keys and long-press keys (4 rows instead of 5 on
  `?123`), with `_` and `/` (or `<` and `>`) next to the space bar.
- Numpad key removed from the symbols bottom row. D-pad key off by default.

### Added
- Unit test `symbols layouts match OpenBoard`.

[custom-v5]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/custom-v5
[custom-v4]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/custom-v4
[custom-v3]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/custom-v3
[custom-v2]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/custom-v2
[custom-v1]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/custom-v1
[openboard-layout-debug-1]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/openboard-layout-debug-1
