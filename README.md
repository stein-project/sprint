# Sprint

A [Stein Loader](https://github.com/Stein-Project) mod for Minecraft 1.8.9: toggle sprint and toggle sneak with a
key, and your movement state on the HUD.

- **Toggle Sprint** (G) and **Toggle Sneak** (no key by default): the mod holds the game's key for you. The game still
  decides when you sprint or sneak (forward only, hunger, items in use, blindness), so the server sees exactly what it
  would see from a player holding the key. Nothing about the movement changes.
- **Sprint state on the HUD**: `[Sprinting (Toggled)]`, `[Sneaking (Key Held)]`, `[Flying + Sprinting (Toggled)]`,
  `[Descending (Toggled)]`, `[Riding]`, in three text styles. By default it shows only what is happening; it can also
  show a toggle while you stand still.
- **Sprint by double-tapping forward** can be turned off, **stop sneaking when you start flying**, and the game's own
  sprint and sneak keys can also toggle.
- Servers can turn either toggle off (`toggleSprint`, `toggleSneak`).
- Toggle sprint used to live in Combat: on the first start, Sprint brings over whether it was on and the key you had
  chosen for it.

For other mods, `dev.xavier.stein.sprint.api.Sprint` exposes the sprint reset after a hit (Combat's W-tap). Declare
`"optional": {"steinsprint": "0.1.0"}` in your `steinmod.json` and check `Mods.isLoaded("steinsprint")` first.

## Download

Sprint comes with the [Stein Client](https://stein-project.github.io/client/). By hand: download `Sprint.steinmod`
from the [latest release](https://github.com/Stein-Project/sprint/releases/latest), put it in `.minecraft/mods` and
play with the **Stein Loader** profile.

## Building

You need **JDK 25 or newer**.
The [Stein SDK](https://github.com/Stein-Project/sdk) Gradle plugin prepares Minecraft 1.8.9 for compiling the
first time, from your own installation or from Mojang.

```sh
./gradlew build            # build/libs/Sprint.steinmod (also checks the formatting)
./gradlew spotlessApply    # format the code (google-java-format, AOSP style) and add the licence header
./gradlew installMod       # build and copy into .minecraft/mods
```

Before a Stein SDK release (developing the SDK and the mod together), build against a local SDK: in a clone of
[sdk](https://github.com/Stein-Project/sdk) run `./gradlew build publishPlugin`, then here
`./gradlew build -Pstein.sdkDir=<path to that clone>` (the plugin comes from its `build/maven-repo`, the SDK from
its `build/sdk/stein-sdk.jar`).

## Contributing

Bug reports, ideas and pull requests are welcome. Read the
[contributing guide](https://github.com/Stein-Project/.github/blob/main/CONTRIBUTING.md) first; your first pull
request asks you to sign the [CLA](https://github.com/Stein-Project/.github/blob/main/CLA.md). For a change to this
mod, also check in the game that sprinting with the toggle behaves exactly like holding the sprint key (walls,
hunger, blindness, eating), that the keys do nothing with a screen open, and that the HUD state is right when flying,
riding and after changing worlds.

Releases are made by the maintainer: a `v<version>` tag, with `changelog/<version>.en_US.md` (and optionally
`.pt_BR.md`) written first. Each stable release carries the mod's index, `steinmods.json` (see
`.github/workflows/release.yml`).

## Licence

Sprint is licensed under the [Mozilla Public License 2.0](LICENSE). Third-party notices are in [NOTICE](NOTICE).
The Stein name and logo are not covered by the licence: see the
[trademark policy](https://github.com/Stein-Project/.github/blob/main/TRADEMARK.md).

Stein is not an official Minecraft product. It is not approved by or associated with Mojang or Microsoft.

Created by [Breno Xavier](https://github.com/brenoxavier).
