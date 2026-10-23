# About HeliBoard

This page describes the app that this custom build is based on: what HeliBoard is, what it can do,
and where to find upstream's own documentation. For what this build changes, see the
[README](../README.md).

## What HeliBoard is

HeliBoard is an open-source keyboard for Android. It is developed by Helium314 and the HeliBoard
contributors at <https://github.com/HeliBorg/HeliBoard>.

- It is based on [OpenBoard](https://github.com/openboard-team/openboard), which is based on the
  AOSP keyboard (the keyboard that comes with Android's source code).
- It has **no internet permission**. Typing, suggestions and learned words stay on the phone.
- It runs on Android 5.0 (API 21) and newer.

The official app is available on F-Droid, IzzyOnDroid and HeliBoard's GitHub Releases page. This
custom build is a separate app (`helium314.keyboard.custom`) and installs next to it.

## Features

All of these work in this custom build too. Where the build behaves differently, it's noted.

- **Dictionaries** for suggestions and spell checking. Some come with the app. More can be loaded
  from a file, for example from Helium314's
  [dictionary collection](https://codeberg.org/Helium314/aosp-dictionaries) (quality varies by
  language). There are also extra dictionaries for emoji and scientific symbols.
- **Multilingual typing**: several languages can be active at once.
- **Layouts**: many languages and layouts are included. Main layouts and the special layouts (symbols,
  numbers, functional keys) can be edited as text files. The format is described in
  [layouts.md](../layouts.md). This build changes the default symbol layouts and bottom rows to
  OpenBoard's (see the README).
- **Themes**: style, colors and background image can be changed. On Android 10 and newer, the theme
  can follow the system's day/night setting. On Android 12 and newer, it can use the system's dynamic
  colors.
- **Toolbar** above the keys with suggestions and tool keys (voice input, clipboard, cursor keys,
  undo, settings and more). In this build it defaults to suggestions only, with voice and clipboard
  pinned (see the README).
- **Clipboard history**, with pinned clips. In this build the clipboard view has no bottom key row.
- **Emoji** keyboard, and emoji search if an emoji dictionary is loaded.
- **One-handed mode**, **split keyboard**, **floating keyboard** and a **number pad**.
- **Glide (gesture) typing** needs a closed-source library that isn't included in the app. You have to
  get it yourself and load it in the settings. Upstream's wiki explains where to find it. In this build
  the gesture trail lifespan goes up to 20 s and each swipe uses a new light color (see the README).
- **Backup and restore** of settings, learned words and history to a file. This is also how you move
  settings between official HeliBoard and this build.

Upstream's wiki lists more, including features that are easy to miss
([hidden features](https://github.com/HeliBorg/HeliBoard/wiki/9.-Hidden-features)).

## Upstream documentation

These are maintained by the HeliBoard project and describe the official app:

- [HeliBoard README](https://github.com/HeliBorg/HeliBoard/blob/main/README.md): upstream's own
  overview, including community links and credits
- [Wiki](https://github.com/HeliBorg/HeliBoard/wiki) and [FAQ](https://github.com/HeliBorg/HeliBoard/wiki/FAQ):
  help with settings, layouts, dictionaries and glide typing
- [Contributing guide](https://github.com/HeliBorg/HeliBoard/blob/main/CONTRIBUTING.md) (also in this
  repository as [CONTRIBUTING.md](../CONTRIBUTING.md)), and the rules for AI-assisted contributions in
  [AI_USAGE.md](../AI_USAGE.md)
- [Translations](https://translate.codeberg.org/projects/heliboard/) are done on Weblate
- [Layout format](../layouts.md) for custom layouts

Upstream's issue tracker and discussions are for the official app. Please don't report problems with
this custom build there.

## License

- HeliBoard is licensed under the [GNU General Public License v3.0](../LICENSE). This custom build
  is a modified version and uses the same license.
- Parts that come from the AOSP keyboard are under the [Apache License 2.0](../LICENSE-Apache-2.0).
- The launcher icon is by [Fabian OvrWrt](https://github.com/FabianOvrWrt) with contributions from
  [The Eclectic Dyslexic](https://github.com/the-eclectic-dyslexic), licensed under
  [CC BY-SA 4.0](../LICENSE-CC-BY-SA-4.0). This build inverts its colors. The changed icon is under
  the same license.
