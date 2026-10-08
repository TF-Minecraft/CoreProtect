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

See [CONTRIBUTING.md](CONTRIBUTING.md) for the repository contribution policies.

## Documentation

[Project documentation](https://github.com/TF-Minecraft/Docs/blob/main/projects/CoreProtect/README.md)

Technical documentation is maintained in [TF-Minecraft/Docs](https://github.com/TF-Minecraft/Docs).

## Tests

Run `mvn clean verify` with Java 21 from `master`. JUnit and Mockito tests cover
custom item and mob labels, MythicMobs identity capture, metadata serialization,
and kill logging. Surefire writes reports to `target/surefire-reports/`, which
CI uploads. No coverage report or minimum coverage gate is configured.

The suite does not exercise a live Paper server, real provider plugins, database
servers, or end-to-end rollback and restore operations.

## License

CoreProtect is distributed under the [Artistic License 2.0](LICENSE).
