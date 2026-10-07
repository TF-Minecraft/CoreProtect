# CoreProtect

> World history and grief recovery for TF-Minecraft.

CoreProtect records changes to the Minecraft world so staff can investigate what happened and recover affected areas. Block history, container transactions, and player activity help turn reports into a clear sequence of events.

This repository is TF-Minecraft's fork of CoreProtect, developed by [PlayPro](https://github.com/PlayPro/CoreProtect).

## Features

- **In-world inspection** — inspect blocks and containers to review their recorded history.
- **Detailed activity searches** — narrow results by player, time, action, location, and affected materials.
- **Targeted recovery** — roll back selected changes and restore previously rolled-back activity.
- **Recovery previews** — review the effect of a rollback or restore before applying it.
- **Broad world coverage** — logging includes player block changes, container contents, explosions, fire, liquids, many entity interactions, skills players use and where they teleport them, and where each online player is once a minute.
- **Custom item and mob labels** — item and chest lookups show saved MMOItems names and IDs; kill lookups show recorded mob names and, for new MythicMobs deaths, the internal mob ID.
- **Staff investigation tools** — paginated lookups, inventory history, and WorldEdit selection support help investigate incidents of different sizes.

CoreProtect supports both investigation and recovery; the recorded history available to staff depends on the server's logging settings.

Named ordinary items are marked as renamed; a display name alone does not prove
MMOItems or MythicMobs identity. These labels also apply to historical records
where the required metadata was saved. MythicLib and MythicMobs integrations are
optional, and the vanilla type remains the fallback. Existing material filters,
aggregate counts and rollback payloads keep their original meaning; custom-ID
search and custom mob restoration are not added by this change.

## Documentation

[Project documentation](https://github.com/TF-Minecraft/Docs/blob/main/projects/CoreProtect/README.md)

Technical documentation is maintained in [TF-Minecraft/Docs](https://github.com/TF-Minecraft/Docs).

## Credits and license

CoreProtect is an upstream [PlayPro project](https://github.com/PlayPro/CoreProtect), distributed under the [Artistic License 2.0](LICENSE). See [CONTRIBUTING.md](CONTRIBUTING.md) for its contribution policies.
