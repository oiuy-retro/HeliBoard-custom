# Changelog

All notable changes to this custom build of HeliBoard are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

All custom releases keep upstream's version name **4.3-beta1** and versionCode **4300**, and are
built from upstream `main` at `e97acdbe` (the `v4.3-beta1` tag). They're published as GitHub
prereleases and told apart by their tag. From `custom-v1` on, they're release builds with the package
`helium314.keyboard.custom`. The earlier test build was a debug build (`helium314.keyboard.debug`).
For changes in the app itself, see the [upstream project](https://github.com/HeliBorg/HeliBoard).

## [Unreleased]

Documentation only. No new APK.

### Changed
- `docs/UPSTREAM_README.md` (a copy of upstream's README) is replaced by
  [docs/ABOUT_HELIBOARD.md](docs/ABOUT_HELIBOARD.md), a short description of HeliBoard in this
  repository's style: what it is, its features (with notes where this build differs), links to
  upstream's README, wiki, FAQ and contributing guide, and the licenses. Links in the README and
  docs are updated.
- README: blockquote and "Building from source" wording aligned with the other custom repositories,
  and the intro now mentions the longer gesture trail.

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

[custom-v1]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/custom-v1
[openboard-layout-debug-1]: https://github.com/oiuy-retro/HeliBoard-custom/releases/tag/openboard-layout-debug-1
