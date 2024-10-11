# FSit (Fabric)

A _server-side_ mod that allows players to sit anywhere!

_Inspired by [GSit][gsit]._

![2024-04-10_20 17 24](https://github.com/rvbsm/fsit/assets/39232658/b500c9d5-7f50-4afd-b293-85f2772d393c)

![2023-08-05_22 04 21](https://github.com/rvbsm/fsit/assets/39232658/9d35e564-8527-4b62-827b-4b8d31dfafd6)

![2023-09-10_14 03 01](https://github.com/rvbsm/fsit/assets/39232658/344aba3c-d7f1-40d7-a9bb-500ccacfdfcb)

## Usage

* Interact with blocks like stairs, slabs, and horizontal logs
* Hop on and ride other players!
* `/sit` and `/crawl` commands for sitting and crawling actions

### Client

* Manageable riders through the Social Interactions screen (1.20.2+)
* Keybindings

![2024-04-10_21 53 42](https://github.com/rvbsm/fsit/assets/39232658/5c9187d3-1faf-4f94-868c-887bc40a1761)

![2024-04-10_21 54 32](https://github.com/rvbsm/fsit/assets/39232658/cfa90e8e-4790-4030-8ae0-36d5ed3d9354)

### Configuration (`config/fsit.yaml`)

If mod is installed on both the server and client,
player configuration will be automatically synced (only if `use_server` is `false`).

The config can be modified with [Mod Menu][modmenu] and [YetAnotherConfigLib][yacl] installed.

```yaml
version: 1
# Whether to use the server-side configuration.
use_server: false
sitting:
  # Controls sitting behaviour. Possible values: nothing, discard (if no block underneath seat), gravity.
  behaviour: "discard"
on_use:
  # Allows to start sitting on specific blocks by interacting with them.
  sitting: true
  # Allows to start riding other players by interaction with them.
  riding: true
  # The maximum distance to a target to interact.
  range: 2
  # Prevents players from sitting in places where they would suffocate.
  check_suffocation: true
  # List of blocks or block types (e.g., "oak_log", "#logs") that are available to sit on by interacting with them.
  blocks:
    - "#minecraft:slabs"
    - "#minecraft:stairs"
    - "#minecraft:logs"
on_double_sneak:
  # Allows to start sitting by double sneaking while looking down.
  sitting: true
  # Allows to start crawling by double sneaking near a one-block gap.
  crawling: true
  # The minimum angle must be looking down (in degrees) with double sneak.
  min_pitch: 60.0
  # The window between sneaks to sit down (in milliseconds).
  delay: 600
```

## Contributing

Pull requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

### Building

Since the project uses [Stonecutter][stonecutter], the build is performed using `chiseledBuild` task.

```shell
> ./gradlew chiseledBuild
```

For further info, visit the [Stonecutter Wiki][stonecutter-wiki].

## License

This project is licensed under the [MIT License](./LICENSE).

[gsit]: https://github.com/Gecolay/GSit

[social-interactions]: https://minecraft.wiki/w/Social_interactions

[modmenu]: [https://modrinth.com/mod/modmenu]
[yacl]: [https://modrinth.com/mod/yacl]

[stonecutter]: https://github.com/kikugie/stonecutter
[stonecutter-wiki]: https://stonecutter.kikugie.dev/stonecutter/introduction
